package io.github.beppe2880.opencodemobile.feature.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.beppe2880.opencodemobile.core.ui.theme.OcRadius
import io.github.beppe2880.opencodemobile.core.ui.theme.OcSpacing
import io.github.beppe2880.opencodemobile.core.ui.theme.OcTheme
import io.github.beppe2880.opencodemobile.feature.chat.resources.Res
import io.github.beppe2880.opencodemobile.feature.chat.resources.chat_untitled
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

/** Share of the width a drag must cover to switch session. */
private const val SWITCH_FRACTION = 0.25f

/** Drag toward a side without a session moves this much, so the edge is felt. */
private const val EDGE_RESISTANCE = 0.2f

/**
 * Horizontal swipe over [content] opens the [previous] (swipe right) or [next] (swipe left)
 * session. Nested horizontal scrollers (code, diffs) consume their drags first.
 */
@Composable
internal fun SessionSwipe(
    previous: SessionPeek?,
    next: SessionPeek?,
    enabled: Boolean,
    onSwitch: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val active = enabled && (previous != null || next != null)
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var width by remember { mutableIntStateOf(1) }
    val threshold = width * SWITCH_FRACTION
    val target = when {
        offset.value <= -threshold -> next
        offset.value >= threshold -> previous
        else -> null
    }
    Box(
        modifier = modifier
            .onSizeChanged { width = it.width.coerceAtLeast(1) }
            .then(
                if (!active) Modifier else Modifier.pointerInput(previous, next, width) {
                    var armed = false
                    detectHorizontalDragGestures(
                        onDragStart = { armed = false },
                        onDragEnd = {
                            val goTo = when {
                                offset.value <= -threshold -> next
                                offset.value >= threshold -> previous
                                else -> null
                            }
                            scope.launch {
                                if (goTo != null) onSwitch(goTo.id) else offset.animateTo(0f)
                            }
                        },
                        onDragCancel = { scope.launch { offset.animateTo(0f) } },
                        onHorizontalDrag = { change, delta ->
                            change.consume()
                            val proposed = offset.value + delta
                            val blocked = (proposed < 0 && next == null) || (proposed > 0 && previous == null)
                            val value = if (blocked) offset.value + delta * EDGE_RESISTANCE else proposed
                            val crossed = abs(value) >= threshold && !blocked
                            if (crossed != armed) {
                                armed = crossed
                                if (crossed) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            scope.launch { offset.snapTo(value) }
                        },
                    )
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offset.value * 0.5f
                    alpha = 1f - (abs(offset.value) / width).coerceIn(0f, 0.5f)
                },
            content = content,
        )
        val peek = if (offset.value < 0) next else if (offset.value > 0) previous else null
        if (peek != null) {
            val progress = (abs(offset.value) / threshold).coerceIn(0f, 1f)
            SwipePeek(
                peek = peek,
                forward = offset.value < 0,
                ready = target != null,
                modifier = Modifier
                    .align(if (offset.value < 0) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = OcSpacing.md)
                    .graphicsLayer { alpha = progress },
            )
        }
    }
}

@Composable
private fun SwipePeek(peek: SessionPeek, forward: Boolean, ready: Boolean, modifier: Modifier = Modifier) {
    val c = OcTheme.colors
    Row(
        modifier = modifier
            .widthIn(max = 240.dp)
            .background(if (ready) c.bgInverse else c.layer02, RoundedCornerShape(OcRadius.lg))
            .padding(horizontal = OcSpacing.md, vertical = OcSpacing.sm)
            .testTag("chat_swipe_peek"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OcSpacing.xs),
    ) {
        val tint = if (ready) c.textInverse else c.textBase
        if (!forward) Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = tint, modifier = Modifier.size(16.dp))
        Text(
            peek.title.ifBlank { stringResource(Res.string.chat_untitled) },
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (forward) Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = tint, modifier = Modifier.size(16.dp))
    }
}
