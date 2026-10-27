package io.github.beppe2880.opencodemobile.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.beppe2880.opencodemobile.core.domain.McpStatus
import io.github.beppe2880.opencodemobile.core.ui.components.OcBottomSheet
import io.github.beppe2880.opencodemobile.core.ui.components.OcEmptyState
import io.github.beppe2880.opencodemobile.core.ui.components.OcErrorState
import io.github.beppe2880.opencodemobile.core.ui.components.OcIconButton
import io.github.beppe2880.opencodemobile.core.ui.components.OcSpinner
import io.github.beppe2880.opencodemobile.core.ui.theme.OcSpacing
import io.github.beppe2880.opencodemobile.core.ui.theme.OcTheme

@Composable
internal fun McpSheet(directory: String, panel: McpPanel, onEvent: (ChatEvent) -> Unit) {
    val c = OcTheme.colors
    OcBottomSheet(onDismiss = { onEvent(ChatEvent.CloseMcps) }, testTag = "chat_mcps_sheet") {
        LazyColumn(
            // A stable viewport keeps the sheet anchor fixed as asynchronous results arrive.
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f),
            contentPadding = PaddingValues(
                start = OcSpacing.lg,
                end = OcSpacing.lg,
                bottom = OcSpacing.lg + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(OcSpacing.md),
        ) {
            item(key = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(OcSpacing.xs)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.chat_mcps), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        if (panel.loading) OcSpinner()
                        OcIconButton(
                            icon = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.chat_mcps_refresh),
                            onClick = { onEvent(ChatEvent.RefreshMcps) },
                            enabled = !panel.loading,
                            modifier = Modifier.testTag("chat_mcps_refresh"),
                        )
                    }
                    Text(directory, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                    Text(stringResource(R.string.chat_mcps_runtime), style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                }
            }
            panel.error?.let { error ->
                item(key = "error") {
                    OcErrorState(
                        title = stringResource(R.string.chat_mcps_error),
                        message = error.resolve(),
                        onRetry = { onEvent(ChatEvent.RefreshMcps) },
                        retryTestTag = "chat_mcps_retry",
                    )
                }
            }
            if (panel.servers?.isEmpty() == true) {
                item(key = "empty") {
                    OcEmptyState(
                        icon = Icons.Outlined.Extension,
                        title = stringResource(R.string.chat_mcps_empty),
                        message = stringResource(R.string.chat_mcps_empty_description),
                        modifier = Modifier.testTag("chat_mcps_empty"),
                    )
                }
            }
            items(panel.servers.orEmpty(), key = { it.name }) { server ->
                val status = stringResource(
                    when (server.status) {
                        McpStatus.Connected -> R.string.chat_mcps_connected
                        McpStatus.Disabled -> R.string.chat_mcps_disconnected
                        McpStatus.Pending -> R.string.chat_mcps_pending
                        McpStatus.Failed -> R.string.chat_mcps_failed
                        McpStatus.NeedsAuth -> R.string.chat_mcps_needs_auth
                    },
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(OcSpacing.sm)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(OcSpacing.xxs)) {
                        Text(server.name, style = MaterialTheme.typography.bodyMedium, color = c.textBase)
                        Text(
                            status,
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMuted,
                            modifier = Modifier.testTag("chat_mcp_status_${server.name}"),
                        )
                        if (server.status == McpStatus.NeedsAuth) {
                            Text(stringResource(R.string.chat_mcps_auth_hint), style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        }
                    }
                    if (server.name in panel.changing) OcSpinner()
                    Switch(
                        checked = server.status == McpStatus.Connected || server.status == McpStatus.Pending,
                        onCheckedChange = { onEvent(ChatEvent.SetMcpConnected(server.name, it)) },
                        enabled = !panel.loading && server.name !in panel.changing,
                        modifier = Modifier.testTag("chat_mcp_toggle_${server.name}").semantics { contentDescription = server.name },
                    )
                }
            }
        }
    }
}
