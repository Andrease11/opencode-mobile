package io.github.beppe2880.opencodemobile.core.ui.text

import androidx.compose.runtime.Composable

/** Integer with the grouping separators of the current locale, e.g. `19,952` or `19.952`. */
expect fun formatInteger(value: Long): String

/** Abbreviated localized relative time at minute resolution, e.g. "5 min ago". */
internal expect fun formatRelativeTime(timestamp: Long, now: Long): String

/** Short localized file size, e.g. "1.2 MB", as the platform shows it. */
@Composable
expect fun formatFileSize(bytes: Long): String
