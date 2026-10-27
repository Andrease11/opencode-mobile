package io.github.beppe2880.opencodemobile.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDuckOthers
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.setActive
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.math.log10
import kotlin.math.sqrt

/** Speech recognition with SFSpeechRecognizer on the microphone stream of an AVAudioEngine. */
@OptIn(ExperimentalForeignApi::class)
@Stable
private class IosDictation : Dictation {
    private val recognizer: SFSpeechRecognizer? = SFSpeechRecognizer()

    override val available: Boolean get() = recognizer?.isAvailable() == true

    override var listening by mutableStateOf(false)
        private set

    override var level by mutableFloatStateOf(0f)
        private set

    private var engine: AVAudioEngine? = null
    private var request: SFSpeechAudioBufferRecognitionRequest? = null
    private var task: SFSpeechRecognitionTask? = null

    /** Incremented on every start and cancel, so callbacks of an older run are ignored. */
    private var run = 0

    override fun start(onText: (String) -> Unit, onFailure: (DictationFailure) -> Unit) {
        val speech = recognizer
        if (speech == null || !speech.isAvailable()) return onFailure(DictationFailure.Unavailable)
        val current = ++run
        listening = true
        SFSpeechRecognizer.requestAuthorization { status ->
            dispatch_async(dispatch_get_main_queue()) {
                if (current != run) return@dispatch_async
                if (status != SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized) {
                    finish()
                    onFailure(DictationFailure.Permission)
                } else if (!listen(speech, current, onText, onFailure)) {
                    teardown()
                    finish()
                    onFailure(DictationFailure.Other)
                }
            }
        }
    }

    private fun listen(
        speech: SFSpeechRecognizer,
        current: Int,
        onText: (String) -> Unit,
        onFailure: (DictationFailure) -> Unit,
    ): Boolean {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryRecord, AVAudioSessionModeMeasurement, AVAudioSessionCategoryOptionDuckOthers, null)
        session.setActive(true, AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, null)

        val audio = AVAudioEngine().also { engine = it }
        val buffers = SFSpeechAudioBufferRecognitionRequest().apply { shouldReportPartialResults = true }.also { request = it }
        val input = audio.inputNode
        val format = input.outputFormatForBus(0u)
        // Without a usable input device (e.g. a simulator on a Mac without microphone) the format
        // is empty and installTap raises an Objective-C exception that would abort the app.
        if (format.sampleRate <= 0.0 || format.channelCount == 0u) return false
        input.installTapOnBus(0u, bufferSize = 1024u, format = format) { buffer, _ ->
            if (buffer == null) return@installTapOnBus
            buffers.appendAudioPCMBuffer(buffer)
            val loudness = buffer.loudness()
            dispatch_async(dispatch_get_main_queue()) { if (current == run && listening) level = loudness }
        }
        audio.prepare()
        if (!audio.startAndReturnError(null)) return false

        var heard = false
        task = speech.recognitionTaskWithRequest(buffers) { result, error ->
            val text = result?.bestTranscription?.formattedString?.takeIf { it.isNotBlank() }
            val final = result?.isFinal() == true
            dispatch_async(dispatch_get_main_queue()) {
                if (current != run) return@dispatch_async
                if (text != null) {
                    heard = true
                    onText(text)
                }
                if (final || error != null) {
                    teardown()
                    finish()
                    if (!heard) onFailure(DictationFailure.NoSpeech)
                }
            }
        }
        return true
    }

    override fun stop() {
        engine?.stop()
        engine?.inputNode?.removeTapOnBus(0u)
        request?.endAudio()
        level = 0f
    }

    override fun cancel() {
        run++
        task?.cancel()
        teardown()
        finish()
    }

    private fun teardown() {
        engine?.stop()
        engine?.inputNode?.removeTapOnBus(0u)
        request?.endAudio()
        engine = null
        request = null
        task = null
        AVAudioSession.sharedInstance().setActive(false, AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, null)
    }

    private fun finish() {
        listening = false
        level = 0f
    }

    private companion object {
        const val MIN_DB = -50f
        const val MAX_DB = -10f

        /** RMS loudness of the first channel, mapped from [MIN_DB]..[MAX_DB] to 0..1. */
        fun AVAudioPCMBuffer.loudness(): Float {
            val samples = floatChannelData?.get(0) ?: return 0f
            val count = frameLength.toInt()
            if (count == 0) return 0f
            var sum = 0.0
            for (i in 0 until count) {
                val sample = samples[i]
                sum += sample * sample
            }
            val rms = sqrt(sum / count)
            val db = if (rms > 0) 20 * log10(rms).toFloat() else MIN_DB
            return ((db - MIN_DB) / (MAX_DB - MIN_DB)).coerceIn(0f, 1f)
        }
    }
}

@Composable
internal actual fun rememberDictation(): Dictation {
    val dictation = remember { IosDictation() }
    DisposableEffect(dictation) { onDispose { dictation.cancel() } }
    return dictation
}

@Composable
internal actual fun rememberMicPermission(onResult: (Boolean) -> Unit): MicPermission {
    val currentOnResult by rememberUpdatedState(onResult)
    return remember {
        object : MicPermission {
            override fun granted() = AVAudioSession.sharedInstance().recordPermission == AVAudioSessionRecordPermissionGranted

            override fun request() {
                AVAudioSession.sharedInstance().requestRecordPermission { granted ->
                    dispatch_async(dispatch_get_main_queue()) { currentOnResult(granted) }
                }
            }
        }
    }
}
