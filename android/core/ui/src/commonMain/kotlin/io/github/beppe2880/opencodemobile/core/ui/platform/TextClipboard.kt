package io.github.beppe2880.opencodemobile.core.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/** Plain-text access to the system clipboard. */
@Stable
interface TextClipboard {
    /** The current clip as text, or null when it is empty or not text. */
    suspend fun getText(): String?

    suspend fun setText(text: String)
}

@Composable
expect fun rememberTextClipboard(): TextClipboard
