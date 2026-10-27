package io.github.beppe2880.opencodemobile.core.ui.components

import androidx.compose.ui.Modifier

/**
 * Exposes test tags as resource IDs for UI Automator / Maestro. Popups, dialogs and sheets are
 * separate composition roots, so each one applies this in addition to the activity root.
 */
expect fun Modifier.exposeTestTags(): Modifier
