package io.github.beppe2880.opencodemobile.core.ui.text

import androidx.compose.runtime.Composable
import io.github.beppe2880.opencodemobile.core.ui.resources.Res
import io.github.beppe2880.opencodemobile.core.ui.resources.oc_just_now
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.floor

/** Compact duration such as `850ms`, `3s`, `1m 20s`, `2h 5m`. */
fun formatDuration(millis: Long): String {
    if (millis < 1_000) return "${millis.coerceAtLeast(0)}ms"
    val seconds = millis / 1_000
    if (seconds < 60) return "${seconds}s"
    val minutes = seconds / 60
    if (minutes < 60) return if (seconds % 60 == 0L) "${minutes}m" else "${minutes}m ${seconds % 60}s"
    val hours = minutes / 60
    return if (minutes % 60 == 0L) "${hours}h" else "${hours}h ${minutes % 60}m"
}

/** `$0.0042`, `$1.23`; empty for zero. */
fun formatCost(cost: Double): String = when {
    cost <= 0.0 -> ""
    cost < 0.01 -> "$" + formatDecimal(cost, 4)
    else -> "$" + formatDecimal(cost, 2)
}

/** `81.5 tok/s`, one decimal like the OpenCode TUI. */
fun formatTokensPerSecond(rate: Double): String = formatDecimal(rate, 1) + " tok/s"

/** Exact count with locale grouping, like the TUI: `19,952 tok` (`19.952 tok` in Italian). */
fun formatTokenTotal(count: Long): String = formatInteger(count) + " tok"

/** `1.2k`, `35k`, `1.4M`, `1.8B` tokens. */
fun formatTokens(count: Long): String = when {
    count < 1_000 -> count.toString()
    count < 10_000 -> formatDecimal(count / 1_000.0, 1) + "k"
    count < 1_000_000 -> "${count / 1_000}k"
    count < 1_000_000_000 -> formatDecimal(count / 1_000_000.0, 1) + "M"
    else -> formatDecimal(count / 1_000_000_000.0, 1) + "B"
}

/**
 * Fixed-point [value] with [decimals] digits, rounding half up and always using `.`,
 * like `String.format(Locale.US, "%.Nf")`.
 */
fun formatDecimal(value: Double, decimals: Int): String {
    var factor = 1L
    repeat(decimals) { factor *= 10 }
    val scaled = floor(abs(value) * factor + 0.5).toLong()
    val sign = if (value < 0 && scaled != 0L) "-" else ""
    val whole = scaled / factor
    if (decimals == 0) return "$sign$whole"
    val fraction = (scaled % factor).toString().padStart(decimals, '0')
    return "$sign$whole.$fraction"
}

/** Localized relative time, e.g. "5 min ago"; "just now" under a minute. */
@Composable
fun relativeTime(timestamp: Long, now: Long): String {
    if (now - timestamp < MINUTE_MILLIS) return stringResource(Res.string.oc_just_now)
    return formatRelativeTime(timestamp, now)
}

private const val MINUTE_MILLIS = 60_000L
