package io.github.beppe2880.opencodemobile.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.beppe2880.opencodemobile.core.ui.R

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_semibold, FontWeight.Bold),
)

val PlexMonoFamily = FontFamily(
    Font(R.font.plex_mono_regular, FontWeight.Normal),
    Font(R.font.plex_mono_medium, FontWeight.Medium),
    Font(R.font.plex_mono_semibold, FontWeight.SemiBold),
    Font(R.font.plex_mono_semibold, FontWeight.Bold),
)

private fun inter(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, tracking: Double = 0.0) = TextStyle(
    fontFamily = InterFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

/** Inter based Material scale, tuned to the dense OpenCode web UI (14sp body). */
val OcMaterialTypography = Typography(
    displayLarge = inter(40, 48, FontWeight.SemiBold, -0.5),
    displayMedium = inter(34, 42, FontWeight.SemiBold, -0.4),
    displaySmall = inter(28, 36, FontWeight.SemiBold, -0.3),
    headlineLarge = inter(26, 34, FontWeight.SemiBold, -0.3),
    headlineMedium = inter(22, 30, FontWeight.SemiBold, -0.2),
    headlineSmall = inter(20, 28, FontWeight.SemiBold, -0.1),
    titleLarge = inter(18, 26, FontWeight.SemiBold),
    titleMedium = inter(16, 24, FontWeight.Medium),
    titleSmall = inter(14, 20, FontWeight.Medium),
    bodyLarge = inter(15, 23),
    bodyMedium = inter(14, 21),
    bodySmall = inter(12, 17),
    labelLarge = inter(14, 20, FontWeight.Medium),
    labelMedium = inter(12, 16, FontWeight.Medium),
    labelSmall = inter(11, 14, FontWeight.Medium, 0.2),
)

/** Monospace styles for code, tool names, paths and commands. */
@Immutable
data class OcMonoTypography(
    val code: TextStyle,
    val codeSmall: TextStyle,
    val label: TextStyle,
)

val OcMono = OcMonoTypography(
    code = TextStyle(fontFamily = PlexMonoFamily, fontSize = 13.sp, lineHeight = 19.sp),
    codeSmall = TextStyle(fontFamily = PlexMonoFamily, fontSize = 12.sp, lineHeight = 17.sp),
    label = TextStyle(fontFamily = PlexMonoFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
)
