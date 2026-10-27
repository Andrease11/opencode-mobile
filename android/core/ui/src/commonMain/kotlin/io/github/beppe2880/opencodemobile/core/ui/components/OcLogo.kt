package io.github.beppe2880.opencodemobile.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.beppe2880.opencodemobile.core.ui.theme.OcTheme

/** OpenCode-style mark: an open square bracket body on an inverse tile. */
@Composable
fun OcLogo(modifier: Modifier = Modifier, size: Dp = 56.dp) {
    val c = OcTheme.colors
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        drawRoundRect(color = c.bgInverse, cornerRadius = CornerRadius(s * 0.22f))
        val w = s * 0.34f
        val h = s * 0.44f
        val left = (s - w) / 2
        val top = (s - h) / 2
        val stroke = s * 0.07f
        drawRect(color = c.textInverse, topLeft = Offset(left, top), size = Size(w, h))
        drawRect(color = c.bgInverse, topLeft = Offset(left + stroke, top + stroke), size = Size(w - 2 * stroke, h - 2 * stroke))
        drawRect(color = c.textFaint, topLeft = Offset(left + stroke, top + h * 0.45f), size = Size(w - 2 * stroke, h * 0.55f - stroke))
    }
}
