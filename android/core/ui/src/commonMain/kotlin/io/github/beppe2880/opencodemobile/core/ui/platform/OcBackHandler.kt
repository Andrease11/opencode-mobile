package io.github.beppe2880.opencodemobile.core.ui.platform

import androidx.compose.runtime.Composable

/** Intercepts the system back action (Android back, iOS edge swipe) while [enabled]. */
@Composable
expect fun OcBackHandler(enabled: Boolean = true, onBack: () -> Unit)
