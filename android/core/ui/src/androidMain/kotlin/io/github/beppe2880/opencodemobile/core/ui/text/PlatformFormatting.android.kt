package io.github.beppe2880.opencodemobile.core.ui.text

import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.text.NumberFormat

actual fun formatInteger(value: Long): String = NumberFormat.getIntegerInstance().format(value)

internal actual fun formatRelativeTime(timestamp: Long, now: Long): String = DateUtils.getRelativeTimeSpanString(
    timestamp,
    now,
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE,
).toString()

@Composable
actual fun formatFileSize(bytes: Long): String = Formatter.formatShortFileSize(LocalContext.current, bytes)
