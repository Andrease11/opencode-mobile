package io.github.beppe2880.opencodemobile.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.beppe2880.opencodemobile.core.ui.resources.Res
import io.github.beppe2880.opencodemobile.core.ui.resources.inter_medium
import io.github.beppe2880.opencodemobile.core.ui.resources.inter_regular
import io.github.beppe2880.opencodemobile.core.ui.resources.inter_semibold
import io.github.beppe2880.opencodemobile.core.ui.resources.plex_mono_medium
import io.github.beppe2880.opencodemobile.core.ui.resources.plex_mono_regular
import io.github.beppe2880.opencodemobile.core.ui.resources.plex_mono_semibold
import org.jetbrains.compose.resources.Font

/** Inter; resource fonts load through composition, so the family is composable. */
@Composable
fun interFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
    Font(Res.font.inter_semibold, FontWeight.Bold),
)

/** IBM Plex Mono for code, tool names, paths and commands. */
@Composable
fun plexMonoFamily(): FontFamily = FontFamily(
    Font(Res.font.plex_mono_regular, FontWeight.Normal),
    Font(Res.font.plex_mono_medium, FontWeight.Medium),
    Font(Res.font.plex_mono_semibold, FontWeight.SemiBold),
    Font(Res.font.plex_mono_semibold, FontWeight.Bold),
)

private fun FontFamily.style(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, tracking: Double = 0.0) = TextStyle(
    fontFamily = this,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

/** Inter based Material scale, tuned to the dense OpenCode web UI (14sp body). */
@Composable
fun ocMaterialTypography(): Typography {
    val inter = interFamily()
    return remember(inter) { inter.materialTypography() }
}

private fun FontFamily.materialTypography() = Typography(
    displayLarge = style(40, 48, FontWeight.SemiBold, -0.5),
    displayMedium = style(34, 42, FontWeight.SemiBold, -0.4),
    displaySmall = style(28, 36, FontWeight.SemiBold, -0.3),
    headlineLarge = style(26, 34, FontWeight.SemiBold, -0.3),
    headlineMedium = style(22, 30, FontWeight.SemiBold, -0.2),
    headlineSmall = style(20, 28, FontWeight.SemiBold, -0.1),
    titleLarge = style(18, 26, FontWeight.SemiBold),
    titleMedium = style(16, 24, FontWeight.Medium),
    titleSmall = style(14, 20, FontWeight.Medium),
    bodyLarge = style(15, 23),
    bodyMedium = style(14, 21),
    bodySmall = style(12, 17),
    labelLarge = style(14, 20, FontWeight.Medium),
    labelMedium = style(12, 16, FontWeight.Medium),
    labelSmall = style(11, 14, FontWeight.Medium, 0.2),
)

/** Monospace styles for code, tool names, paths and commands. */
@Immutable
data class OcMonoTypography(
    val code: TextStyle,
    val codeSmall: TextStyle,
    val label: TextStyle,
)

@Composable
fun ocMonoTypography(): OcMonoTypography {
    val plexMono = plexMonoFamily()
    return remember(plexMono) {
        OcMonoTypography(
            code = TextStyle(fontFamily = plexMono, fontSize = 13.sp, lineHeight = 19.sp),
            codeSmall = TextStyle(fontFamily = plexMono, fontSize = 12.sp, lineHeight = 17.sp),
            label = TextStyle(fontFamily = plexMono, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
        )
    }
}
