package io.github.beppe2880.opencodemobile.core.network

import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SseReaderTest {

    @Test
    fun `joins multi-line data and skips heartbeats and other fields`() = runBlocking {
        val reader = reader(
            ": heartbeat\n\n" +
                "event: message\nid: 1\ndata: {\"a\":\ndata: 1}\n\n" +
                ": heartbeat\n\n" +
                "data:no-space\r\n\r\n",
        )

        assertEquals("{\"a\":\n1}", reader.next())
        assertEquals("no-space", reader.next())
        assertNull(reader.next())
    }

    @Test
    fun `reassembles events split across arbitrary chunks`() = runBlocking {
        val stream = "data: {\"type\":\"one\"}\n\n: ping\n\ndata: {\"type\":\"two\"}\n\n"
        val channel = ByteChannel()
        // Delivers at most 3 bytes per write, like a slow network.
        launch {
            stream.chunked(3).forEach { channel.writeStringUtf8(it); channel.flush() }
            channel.flushAndClose()
        }
        val reader = SseReader(channel)

        assertEquals("{\"type\":\"one\"}", reader.next())
        assertEquals("{\"type\":\"two\"}", reader.next())
        assertNull(reader.next())
    }

    @Test
    fun `discards an event left unterminated at end of stream`() = runBlocking {
        val reader = reader("data: complete\n\ndata: partial")

        assertEquals("complete", reader.next())
        assertNull(reader.next())
    }

    @Test
    fun `empty data line still yields an event`() = runBlocking {
        assertEquals("", reader("data\n\n").next())
    }

    private fun reader(text: String) = SseReader(ByteReadChannel(text))
}
