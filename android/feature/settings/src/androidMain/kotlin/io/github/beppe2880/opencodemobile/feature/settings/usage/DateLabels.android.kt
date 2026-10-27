package io.github.beppe2880.opencodemobile.feature.settings.usage

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

internal actual fun formatMediumDate(date: LocalDate): String =
    date.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))

internal actual fun formatShortDayName(date: LocalDate): String =
    date.toJavaLocalDate().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
