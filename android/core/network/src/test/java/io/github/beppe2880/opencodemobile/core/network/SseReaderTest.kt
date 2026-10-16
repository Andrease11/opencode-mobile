package io.github.beppe2880.opencodemobile.core.network

import okio.Buffer
import okio.Source
import okio.Timeout
import okio.buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SseReaderTest {

    @Test
    fun `joins multi-line data and skips heartbeats and other fields`() {
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
    fun `reassembles events split across arbitrary chunks`() {
        val stream = "data: {\"type\":\"one\"}\n\n: ping\n\ndata: {\"type\":\"two\"}\n\n"
        val reader = SseReader(ChunkedSource(stream, chunkSize = 3).buffer())

        assertEquals("{\"type\":\"one\"}", reader.next())
        assertEquals("{\"type\":\"two\"}", reader.next())
        assertNull(reader.next())
    }

    @Test
    fun `discards an event left unterminated at end of stream`() {
        val reader = reader("data: complete\n\ndata: partial")

        assertEquals("complete", reader.next())
        assertNull(reader.next())
    }

    @Test
    fun `empty data line still yields an event`() {
        assertEquals("", reader("data\n\n").next())
    }

    private fun reader(text: String) = SseReader(Buffer().writeUtf8(text))

    /** Delivers at most [chunkSize] bytes per read, like a slow network. */
    private class ChunkedSource(text: String, private val chunkSize: Long) : Source {
        private val data = Buffer().writeUtf8(text)
        override fun read(sink: Buffer, byteCount: Long): Long =
            if (data.size == 0L) -1 else data.read(sink, minOf(byteCount, chunkSize))
        override fun timeout(): Timeout = Timeout.NONE
        override fun close() = Unit
    }
}
