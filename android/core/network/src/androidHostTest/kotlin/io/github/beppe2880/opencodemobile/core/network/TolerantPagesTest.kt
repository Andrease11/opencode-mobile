package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.api.models.SessionMessagesResponse
import io.github.beppe2880.opencodemobile.core.domain.Message
import io.github.beppe2880.opencodemobile.core.domain.Part
import io.github.beppe2880.opencodemobile.core.network.mapping.toDomain
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TolerantPagesTest {

    private val skipped = mutableListOf<Triple<String, Int, List<String>>>()
    private val defaultOnSkipped = TolerantPages.onSkipped

    @Before
    fun setUp() {
        TolerantPages.onSkipped = { kind, count, types -> skipped += Triple(kind, count, types) }
    }

    @After
    fun tearDown() {
        TolerantPages.onSkipped = defaultOnSkipped
    }

    private fun unknownPage() = TolerantPages.messages(OpenCodeJson, Fixtures.text("session_messages_unknown.json"))

    @Test
    fun `an unknown message type is skipped and the rest of the transcript loads`() {
        val page = unknownPage()

        assertEquals(5, page.data.size)
        // The page size still counts the skipped entry, so paging is not cut short.
        assertEquals(6, page.data.receivedCount)
        assertEquals(listOf(Triple("messages", 1, listOf("from-the-future"))), skipped)
        assertTrue(page.cursor.next != null)

        val messages = page.data.mapNotNull { it.toDomain() }
        assertEquals(
            listOf(Message.User::class, Message.Assistant::class, Message.Assistant::class, Message.Idle::class),
            messages.map { it::class },
        )
    }

    @Test
    fun `an unknown assistant part is dropped and its message kept`() {
        val tools = unknownPage().data.mapNotNull { it.toDomain() }[1] as Message.Assistant

        assertEquals(3, tools.parts.size)
        assertTrue(tools.parts[0] is Part.Reasoning)
        assertEquals(listOf("read", "shell"), tools.parts.drop(1).map { (it as Part.Tool).name })
    }

    @Test
    fun `an unknown enum value in an optional field falls back to its default`() {
        val answer = unknownPage().data.mapNotNull { it.toDomain() }[2] as Message.Assistant

        assertNull(answer.finish)
        assertEquals(1791910789784, answer.completedAt)
    }

    @Test
    fun `known pages decode exactly like the generated serializer`() {
        val text = Fixtures.text("session_messages.json")
        val tolerant = TolerantPages.messages(OpenCodeJson, text)
        val strict = OpenCodeJson.decodeFromString(SessionMessagesResponse.serializer(), text)

        assertEquals(strict, tolerant)
        assertEquals(strict.data.size, tolerant.data.receivedCount)
        assertTrue(skipped.isEmpty())
    }

    @Test
    fun `an undecodable session is skipped from the list`() {
        val good = Fixtures.text("session_list.json")
        val root = OpenCodeJson.parseToJsonElement(good).toString()
        // A second item without the required fields of a session.
        val body = root.replaceFirst("\"data\":[", "\"data\":[{\"type\":\"workspace\",\"id\":1},")

        val page = TolerantPages.sessions(OpenCodeJson, body)

        assertEquals(listOf("ses_ec9cb5c70ffe3M22jWcPWdSAIF"), page.data.map { it.toDomain().id })
        assertEquals(2, page.data.receivedCount)
        assertEquals(listOf(Triple("sessions", 1, listOf("workspace"))), skipped)
    }

    @Test
    fun `the client decodes message pages tolerantly`() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(Fixtures.text("session_messages_unknown.json")))
            val client = OpenCodeClientFactory().create(server.url("/").toString(), null)

            val page = awaitBody { client.sessions.sessionMessageList("ses_1") }

            assertEquals(5, page.data.size)
            assertEquals(6, page.data.receivedCount)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `unknown event types are ignored by the event stream`() {
        assertNull(decodeEvent("""{"id":"evt_1","type":"session.from-the-future","data":{}}"""))
        assertNull(decodeEvent("""["not an object"]"""))
    }
}
