package io.github.beppe2880.opencodemobile.core.ui.components

import androidx.compose.ui.Modifier

// iOS accessibility already exposes test tags as identifiers.
actual fun Modifier.exposeTestTags(): Modifier = this
