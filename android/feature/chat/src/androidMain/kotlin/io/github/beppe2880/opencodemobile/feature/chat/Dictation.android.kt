package io.github.beppe2880.opencodemobile.feature.chat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Voice input through the system speech recognizer (usually Google's): the words stream in
 * as partial results while the user speaks. The recognizer is created on the first use and
 * released with the composition.
 */
@Stable
private class AndroidDictation(private val context: Context) : Dictation {
    override val available: Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    override var listening by mutableStateOf(false)
        private set

    override var level by mutableFloatStateOf(0f)
        private set

    private var recognizer: SpeechRecognizer? = null

    override fun start(onText: (String) -> Unit, onFailure: (DictationFailure) -> Unit) {
        if (!available) return onFailure(DictationFailure.Unavailable)
        val speech = recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also { recognizer = it }
        var heard = false
        speech.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            override fun onRmsChanged(rmsdB: Float) {
                level = ((rmsdB - MIN_DB) / (MAX_DB - MIN_DB)).coerceIn(0f, 1f)
            }

            override fun onEndOfSpeech() {
                level = 0f
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults.bestText()?.let {
                    heard = true
                    onText(it)
                }
            }

            override fun onResults(results: Bundle?) {
                finish()
                results.bestText()?.let {
                    heard = true
                    onText(it)
                }
                if (!heard) onFailure(DictationFailure.NoSpeech)
            }

            override fun onError(error: Int) {
                finish()
                // Stopping after some words were heard ends with "no match" on some engines.
                if (heard && error in QUIET_ERRORS) return
                onFailure(
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> DictationFailure.NoSpeech
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> DictationFailure.Permission
                        SpeechRecognizer.ERROR_CLIENT -> return
                        else -> DictationFailure.Other
                    },
                )
            }
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        listening = true
        speech.startListening(intent)
    }

    override fun stop() {
        recognizer?.stopListening()
    }

    override fun cancel() {
        recognizer?.cancel()
        finish()
    }

    fun release() {
        recognizer?.destroy()
        recognizer = null
        finish()
    }

    private fun finish() {
        listening = false
        level = 0f
    }

    private fun Bundle?.bestText(): String? =
        this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.takeIf { it.isNotBlank() }

    private companion object {
        const val MIN_DB = -2f
        const val MAX_DB = 10f
        val QUIET_ERRORS = setOf(SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT, SpeechRecognizer.ERROR_CLIENT)
    }
}

@Composable
internal actual fun rememberDictation(): Dictation {
    val context = LocalContext.current
    val dictation = remember(context) { AndroidDictation(context) }
    DisposableEffect(dictation) { onDispose { dictation.release() } }
    return dictation
}

@Composable
internal actual fun rememberMicPermission(onResult: (Boolean) -> Unit): MicPermission {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)
    return object : MicPermission {
        override fun granted() = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        override fun request() = launcher.launch(Manifest.permission.RECORD_AUDIO)
    }
}
