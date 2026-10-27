package io.github.beppe2880.opencodemobile.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/** Why dictation stopped without text. */
internal enum class DictationFailure { NoSpeech, Permission, Unavailable, Other }

/**
 * Voice input through the system speech recognizer: the words stream in as partial results
 * while the user speaks. The recognizer is created on the first use and released with the
 * composition.
 */
@Stable
internal interface Dictation {
    val available: Boolean
    val listening: Boolean

    /** Input loudness from 0 to 1 while listening. */
    val level: Float

    /** [onText] gets the whole text heard so far, each time it grows; [onFailure] when nothing was heard. */
    fun start(onText: (String) -> Unit, onFailure: (DictationFailure) -> Unit)

    /** Stops listening; the text heard so far is still delivered. */
    fun stop()

    /** Stops without delivering more text, e.g. when the prompt is sent. */
    fun cancel()
}

@Composable
internal expect fun rememberDictation(): Dictation

/** Microphone permission, asked on the first tap of the microphone. */
@Stable
internal interface MicPermission {
    fun granted(): Boolean

    /** Asks for the permission; the answer goes to the callback of [rememberMicPermission]. */
    fun request()
}

@Composable
internal expect fun rememberMicPermission(onResult: (Boolean) -> Unit): MicPermission

/** Appends dictated words to what was already typed. */
internal fun appendDictated(typed: String, spoken: String): String =
    if (typed.isBlank()) spoken else typed.trimEnd() + " " + spoken

/** Voice input as the composer sees it; null hides the microphone. */
@Immutable
data class VoiceInput(val listening: Boolean, val level: Float)
