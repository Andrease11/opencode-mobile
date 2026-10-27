package io.github.beppe2880.opencodemobile.core.network

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanScannerTest {

    private val servers = mutableListOf<MockWebServer>()

    @After
    fun tearDown() = servers.forEach { it.shutdown() }

    private fun server(manifestName: String?): MockWebServer = MockWebServer().apply {
        dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                manifestName == null -> MockResponse().setResponseCode(401)
                request.path == "/site.webmanifest" ->
                    MockResponse().setHeader("Content-Type", "application/json").setBody("""{"name":"$manifestName","short_name":"x"}""")
                else -> MockResponse().setResponseCode(404)
            }
        }
        start()
        servers += this
    }

    @Test
    fun `only servers whose manifest is OpenCode are found`() = runBlocking {
        val opencode = server("OpenCode")
        val other = server("Grafana")
        val locked = server(null)
        val scanner = LanScanner()

        assertTrue(scanner.isOpenCode(opencode.url("/").toString().trimEnd('/')))
        assertEquals(false, scanner.isOpenCode(other.url("/").toString().trimEnd('/')))
        assertEquals(false, scanner.isOpenCode(locked.url("/").toString().trimEnd('/')))
        assertEquals(listOf("http://127.0.0.1:${opencode.port}"), scanner.scan(listOf("127.0.0.1"), opencode.port).toList())
    }

    @Test
    fun `a closed port is not found`() = runBlocking {
        val port = server("OpenCode").let { val p = it.port; it.shutdown(); p }
        assertTrue(LanScanner().scan(listOf("127.0.0.1"), port).toList().isEmpty())
    }

    @Test
    fun `subnet hosts of a 24 bit network skip network and broadcast`() {
        val hosts = LanScanner.subnetHosts(byteArrayOf(192.toByte(), 168.toByte(), 1, 37), 24)
        assertEquals(254, hosts.size)
        assertEquals("192.168.1.1", hosts.first())
        assertEquals("192.168.1.254", hosts.last())
    }

    @Test
    fun `larger subnets are narrowed to the 24 bit network around the address`() {
        val hosts = LanScanner.subnetHosts(byteArrayOf(10, 0, 5, 9), 16)
        assertEquals("10.0.5.1", hosts.first())
        assertEquals("10.0.5.254", hosts.last())
    }

    @Test
    fun `smaller subnets keep their size and point to point links are skipped`() {
        assertEquals(
            listOf("192.168.1.65", "192.168.1.66"),
            LanScanner.subnetHosts(byteArrayOf(192.toByte(), 168.toByte(), 1, 66), 30),
        )
        assertTrue(LanScanner.subnetHosts(byteArrayOf(10, 8, 0, 2), 32).isEmpty())
        assertTrue(LanScanner.subnetHosts(byteArrayOf(10, 8, 0, 2), 31).isEmpty())
    }
}
