package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.core.domain.McpStatus
import io.github.beppe2880.opencodemobile.core.domain.OpenCodeError
import io.github.beppe2880.opencodemobile.core.network.mapping.toDomain
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class McpApiTest {
    private val server = MockWebServer()
    private val client = OpenCodeClientFactory().create(server.url("/").toString(), BasicCredentials(password = "test"))

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `all MCP states decode and map without exposing raw error details`() = runBlocking {
        val states = listOf("connected", "pending", "disabled", "failed", "needs_auth")
        val entries = states.joinToString(",") { status ->
            val error = if (status in listOf("failed", "needs_auth")) ",\"error\":\"private details\"" else ""
            """{"name":"$status","status":{"status":"$status"$error}}"""
        }
        server.enqueue(MockResponse().setBody("""{"location":{"directory":"/repo"},"data":[$entries]}"""))
        val result = awaitBody { client.mcps.mcpList("/repo with spaces") }.data.map { it.toDomain() }
        assertEquals(listOf(McpStatus.Connected, McpStatus.Pending, McpStatus.Disabled, McpStatus.Failed, McpStatus.NeedsAuth), result.map { it.status })
        assertTrue(result.none { it.toString().contains("private details") })
        val request = server.takeRequest()
        assertEquals("/repo with spaces", request.requestUrl!!.queryParameter("location[directory]"))
        assertEquals("/api/mcp", request.requestUrl!!.encodedPath)
    }

    @Test
    fun `MCP toggles use runtime endpoints and preserve project scope`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(MockResponse().setResponseCode(204))
        awaitSuccess { client.mcps.experimentalMcpConnect("my server", "/repo") }
        awaitSuccess { client.mcps.experimentalMcpDisconnect("my server", "/repo") }
        listOf("connect", "disconnect").forEach { action ->
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/experimental/mcp/my%20server/$action", request.requestUrl!!.encodedPath)
            assertEquals("/repo", request.requestUrl!!.queryParameter("location[directory]"))
        }
    }

    @Test
    fun `MCP endpoint errors are typed rather than treated as success`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"_tag":"UnauthorizedError","message":"Authentication required"}"""))
        val failure = runCatching { awaitSuccess { client.mcps.experimentalMcpDisconnect("test", "/repo") } }.exceptionOrNull()
        assertTrue(failure is OpenCodeError.Unauthorized)
    }
}
