package io.github.beppe2880.opencodemobile.core.network.mapping

import io.github.beppe2880.opencodemobile.api.models.McpServerStatus
import io.github.beppe2880.opencodemobile.core.domain.McpServer
import io.github.beppe2880.opencodemobile.core.domain.McpStatus
import io.github.beppe2880.opencodemobile.api.models.McpServer as ApiMcpServer

/** Do not expose raw server error text, which may contain credentials or configuration. */
fun ApiMcpServer.toDomain(): McpServer = McpServer(
    name = name,
    status = when (status) {
        is McpServerStatus.ConnectedWrapper -> McpStatus.Connected
        is McpServerStatus.DisabledWrapper -> McpStatus.Disabled
        is McpServerStatus.FailedWrapper -> McpStatus.Failed
        is McpServerStatus.NeedsAuthWrapper -> McpStatus.NeedsAuth
        is McpServerStatus.PendingWrapper -> McpStatus.Pending
    },
)
