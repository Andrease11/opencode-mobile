package io.github.beppe2880.opencodemobile.core.ui.components

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

/**
 * Exposes test tags as resource IDs for UI Automator / Maestro. Popups, dialogs and sheets are
 * separate composition roots, so each one applies this in addition to the activity root.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.exposeTestTags(): Modifier = semantics { testTagsAsResourceId = true }
