package io.github.beppe2880.opencodemobile.core.ui.text

import android.text.format.DateUtils
import java.text.NumberFormat

actual fun formatInteger(value: Long): String = NumberFormat.getIntegerInstance().format(value)

internal actual fun formatRelativeTime(timestamp: Long, now: Long): String = DateUtils.getRelativeTimeSpanString(
    timestamp,
    now,
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE,
).toString()
