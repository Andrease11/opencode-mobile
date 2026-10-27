package io.github.beppe2880.opencodemobile.core.data

import io.github.beppe2880.opencodemobile.core.data.mcp.RemoteMcpRepository
import io.github.beppe2880.opencodemobile.core.domain.McpServer
import io.github.beppe2880.opencodemobile.core.domain.McpStatus
import io.github.beppe2880.opencodemobile.core.domain.OpenCodeError
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoteMcpRepositoryTest {
    private val server = MockWebServer()
    private lateinit var mcps: RemoteMcpRepository

    @Before
    fun setUp() {
        server.start()
        mcps = RemoteMcpRepository(TestGraph.activeOn(server).clients)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `list is project scoped sorted and never cached`() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"location":{"directory":"/repo"},"data":[
                    {"name":"z","status":{"status":"connected"}},
                    {"name":"a","status":{"status":"disabled"}}
                ]}""",
            ),
        )
        server.enqueue(MockResponse().setBody("""{"location":{"directory":"/repo"},"data":[]}"""))
        assertEquals(listOf(McpServer("a", McpStatus.Disabled), McpServer("z", McpStatus.Connected)), mcps.list("/repo").getOrThrow())
        assertEquals(emptyList<McpServer>(), mcps.list("/repo").getOrThrow())
        repeat(2) { assertEquals("/repo", server.takeRequest().requestUrl!!.queryParameter("location[directory]")) }
    }

    @Test
    fun `connection changes call the matching endpoint with the same project`() = runBlocking {
        repeat(2) { server.enqueue(MockResponse().setResponseCode(204)) }
        assertTrue(mcps.setConnected("/repo", "test", false).isSuccess)
        assertTrue(mcps.setConnected("/repo", "test", true).isSuccess)
        listOf("disconnect", "connect").forEach { action ->
            val request = server.takeRequest()
            assertEquals("/api/experimental/mcp/test/$action", request.requestUrl!!.encodedPath)
            assertEquals("/repo", request.requestUrl!!.queryParameter("location[directory]"))
        }
    }

    @Test
    fun `failures remain typed results rather than an empty list or successful toggle`() = runBlocking {
        repeat(2) {
            server.enqueue(MockResponse().setResponseCode(401).setBody("""{"_tag":"UnauthorizedError","message":"Authentication required"}"""))
        }
        assertTrue(mcps.list("/repo").exceptionOrNull() is OpenCodeError.Unauthorized)
        assertTrue(mcps.setConnected("/repo", "test", true).exceptionOrNull() is OpenCodeError.Unauthorized)
    }
}
