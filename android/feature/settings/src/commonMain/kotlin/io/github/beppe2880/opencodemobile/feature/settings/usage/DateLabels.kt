package io.github.beppe2880.opencodemobile.feature.settings.usage

import kotlinx.datetime.LocalDate

/** `Oct 12, 2026` in the device language (medium date style). */
internal expect fun formatMediumDate(date: LocalDate): String

/** Abbreviated weekday such as `Mon` in the device language. */
internal expect fun formatShortDayName(date: LocalDate): String
