package io.github.beppe2880.opencodemobile.core.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIPasteboard

/** Reads and writes `UIPasteboard.general` directly; iOS asks the user before the app reads it. */
@Composable
actual fun rememberTextClipboard(): TextClipboard = remember {
    object : TextClipboard {
        override suspend fun getText(): String? = UIPasteboard.generalPasteboard.string

        override suspend fun setText(text: String) {
            UIPasteboard.generalPasteboard.string = text
        }
    }
}
