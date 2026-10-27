package io.github.beppe2880.opencodemobile

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

@Composable
internal actual fun Modifier.dismissKeyboardOnTap(): Modifier {
    val focus = LocalFocusManager.current
    // Taps consumed by buttons, rows and text fields never reach this handler.
    return pointerInput(focus) { detectTapGestures(onTap = { focus.clearFocus() }) }
}
