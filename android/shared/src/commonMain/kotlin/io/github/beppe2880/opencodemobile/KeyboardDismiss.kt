package io.github.beppe2880.opencodemobile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Hides the keyboard when the user taps outside any control. iOS has no Back key to close it;
 * Android keeps the platform behavior.
 */
@Composable
internal expect fun Modifier.dismissKeyboardOnTap(): Modifier
