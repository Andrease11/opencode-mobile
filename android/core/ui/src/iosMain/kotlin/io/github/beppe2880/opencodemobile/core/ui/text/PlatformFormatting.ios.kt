package io.github.beppe2880.opencodemobile.core.ui.text

import androidx.compose.runtime.Composable
import platform.Foundation.NSByteCountFormatter
import platform.Foundation.NSByteCountFormatterCountStyleFile
import platform.Foundation.NSDate
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSRelativeDateTimeFormatter
import platform.Foundation.NSRelativeDateTimeFormatterUnitsStyleAbbreviated
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun formatInteger(value: Long): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        maximumFractionDigits = 0u
    }
    return formatter.stringFromNumber(NSNumber(longLong = value)) ?: value.toString()
}

internal actual fun formatRelativeTime(timestamp: Long, now: Long): String {
    val formatter = NSRelativeDateTimeFormatter().apply {
        unitsStyle = NSRelativeDateTimeFormatterUnitsStyleAbbreviated
    }
    // Minute resolution, like Android's DateUtils.MINUTE_IN_MILLIS.
    val minutes = (now - timestamp) / 60_000L
    return formatter.localizedStringForDate(
        NSDate.dateWithTimeIntervalSince1970((now - minutes * 60_000L) / 1000.0),
        relativeToDate = NSDate.dateWithTimeIntervalSince1970(now / 1000.0),
    )
}

@Composable
actual fun formatFileSize(bytes: Long): String = NSByteCountFormatter.stringFromByteCount(bytes, NSByteCountFormatterCountStyleFile)
