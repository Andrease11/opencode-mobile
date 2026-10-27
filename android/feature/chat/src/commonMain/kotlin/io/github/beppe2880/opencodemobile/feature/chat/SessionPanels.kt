package io.github.beppe2880.opencodemobile.feature.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.beppe2880.opencodemobile.core.ui.components.OcBottomSheet
import io.github.beppe2880.opencodemobile.core.ui.components.OcButton
import io.github.beppe2880.opencodemobile.core.ui.components.OcButtonVariant
import io.github.beppe2880.opencodemobile.core.ui.components.OcSpinner
import io.github.beppe2880.opencodemobile.core.ui.text.formatInteger
import io.github.beppe2880.opencodemobile.core.ui.text.formatTokens
import io.github.beppe2880.opencodemobile.core.ui.theme.OcSpacing
import io.github.beppe2880.opencodemobile.core.ui.theme.OcTheme
import io.github.beppe2880.opencodemobile.feature.chat.resources.Res
import io.github.beppe2880.opencodemobile.feature.chat.resources.chat_compact
import io.github.beppe2880.opencodemobile.feature.chat.resources.chat_compacting
import io.github.beppe2880.opencodemobile.feature.chat.resources.chat_context
import io.github.beppe2880.opencodemobile.feature.chat.resources.chat_context_hint
import io.github.beppe2880.opencodemobile.feature.chat.resources.chat_context_of
import io.github.beppe2880.opencodemobile.feature.chat.resources.chat_context_open
import io.github.beppe2880.opencodemobile.feature.chat.resources.chat_context_tokens
import org.jetbrains.compose.resources.stringResource

/** Compact context gauge for the chat header: `31%`, or the token count when the window is unknown. */
@Composable
internal fun ContextIndicator(context: ContextUsage, compacting: Boolean, onClick: () -> Unit) {
    val c = OcTheme.colors
    val percent = context.percent
    val label = percent?.let { "$it%" } ?: formatTokens(context.tokens)
    val color = when {
        percent == null -> c.textMuted
        percent >= 90 -> c.danger.fg
        percent >= 70 -> c.warning.fg
        else -> c.textMuted
    }
    val description = stringResource(Res.string.chat_context_open, label)
    Row(
        modifier = Modifier
            .padding(end = OcSpacing.xs)
            .clickable(onClick = onClick)
            .heightIn(min = 40.dp)
            .padding(horizontal = OcSpacing.sm)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .testTag("chat_context_indicator"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OcSpacing.xs),
    ) {
        if (compacting) {
            OcSpinner(size = 10.dp)
        } else if (percent != null) {
            LinearProgressIndicator(
                progress = { (percent / 100f).coerceIn(0f, 1f) },
                color = color,
                trackColor = c.layer03,
                modifier = Modifier.size(width = 24.dp, height = 4.dp),
            )
        }
        Text(label, style = OcTheme.mono.label, color = color)
    }
}

/** Context details with the compact action. */
@Composable
internal fun ContextSheet(context: ContextUsage?, compacting: Boolean, onCompact: () -> Unit, onDismiss: () -> Unit) {
    val c = OcTheme.colors
    OcBottomSheet(onDismiss = onDismiss, testTag = "chat_context_sheet") {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = OcSpacing.lg)
                .padding(bottom = OcSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(OcSpacing.sm),
        ) {
            Text(stringResource(Res.string.chat_context), style = MaterialTheme.typography.titleSmall, color = c.textBase)
            if (context != null) {
                Text(
                    stringResource(Res.string.chat_context_tokens, formatInteger(context.tokens)),
                    style = MaterialTheme.typography.titleLarge,
                    color = c.textBase,
                    modifier = Modifier.testTag("chat_context_tokens"),
                )
                val percent = context.percent
                if (percent != null && context.limit != null) {
                    LinearProgressIndicator(
                        progress = { (percent / 100f).coerceIn(0f, 1f) },
                        color = c.textBase,
                        trackColor = c.layer03,
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                    )
                    Text(
                        stringResource(Res.string.chat_context_of, percent, formatInteger(context.limit)),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMuted,
                    )
                }
            }
            Text(stringResource(Res.string.chat_context_hint), style = MaterialTheme.typography.bodySmall, color = c.textMuted)
            OcButton(
                text = stringResource(if (compacting) Res.string.chat_compacting else Res.string.chat_compact),
                onClick = onCompact,
                variant = OcButtonVariant.Secondary,
                loading = compacting,
                modifier = Modifier.fillMaxWidth().testTag("chat_compact_button"),
            )
        }
    }
}
