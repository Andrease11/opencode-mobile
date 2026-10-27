package io.github.beppe2880.opencodemobile.core.data.mcp

import io.github.beppe2880.opencodemobile.core.data.OpenCodeClients
import io.github.beppe2880.opencodemobile.core.data.resultOf
import io.github.beppe2880.opencodemobile.core.domain.McpRepository
import io.github.beppe2880.opencodemobile.core.domain.McpServer
import io.github.beppe2880.opencodemobile.core.network.awaitBody
import io.github.beppe2880.opencodemobile.core.network.awaitSuccess
import io.github.beppe2880.opencodemobile.core.network.mapping.toDomain

internal class RemoteMcpRepository(private val clients: OpenCodeClients) : McpRepository {
    override suspend fun list(directory: String): Result<List<McpServer>> = resultOf {
        awaitBody { clients.active().mcps.mcpList(locationDirectory = directory) }.data
            .map { it.toDomain() }.sortedBy { it.name }
    }

    override suspend fun setConnected(directory: String, name: String, connected: Boolean): Result<Unit> = resultOf {
        val api = clients.active().mcps
        awaitSuccess {
            if (connected) api.experimentalMcpConnect(name, locationDirectory = directory)
            else api.experimentalMcpDisconnect(name, locationDirectory = directory)
        }
    }
}
