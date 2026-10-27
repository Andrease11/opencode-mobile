package io.github.beppe2880.opencodemobile.core.ui.platform

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberTextClipboard(): TextClipboard {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    return remember(clipboard, context) {
        object : TextClipboard {
            override suspend fun getText(): String? =
                clipboard.getClipEntry()?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()

            override suspend fun setText(text: String) {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(CLIP_LABEL, text)))
            }
        }
    }
}

private const val CLIP_LABEL = "OpenCode"
