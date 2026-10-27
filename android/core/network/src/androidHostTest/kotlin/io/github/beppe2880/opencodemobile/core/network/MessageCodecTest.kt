package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.api.models.SessionMessageInfo
import io.github.beppe2880.opencodemobile.core.domain.ImageAttachment
import io.github.beppe2880.opencodemobile.core.domain.Message
import io.github.beppe2880.opencodemobile.core.network.mapping.toDomain
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageCodecTest {

    @Test
    fun `stored messages drop inline images and cap huge text but still decode`() {
        val image = "data:image/png;base64," + "A".repeat(3 * 1024 * 1024)
        val log = "build output ".repeat(20_000)
        val wire = """
            {"id":"msg_1","time":{"created":1,"completed":2},"type":"assistant","agent":"build",
             "model":{"id":"m","providerID":"p"},"content":[{"type":"tool","id":"t1","name":"read",
             "state":{"status":"completed","input":{"path":"shot.png"},
             "content":[{"type":"text","text":"$log"},{"type":"file","uri":"$image","mime":"image/png","name":"shot.png"}]},
             "time":{"created":1,"completed":2}}]}
        """.trimIndent()
        val message = OpenCodeJson.decodeFromString(SessionMessageInfo.serializer(), wire)

        val stored = MessageCodec.encode(message)

        assertEquals("msg_1", stored.id)
        assertTrue("stored ${stored.json.length} chars", stored.json.length < 100_000)
        assertTrue(stored.json.contains("\"data:image/png;base64,\""))
        assertTrue(stored.json.contains("characters not stored"))
        assertNotNull(MessageCodec.decode(stored.json))
    }

    @Test
    fun `stored prompt images keep their place with an empty payload`() {
        val data = "iVBORw0KGgo" + "A".repeat(300_000)
        val wire = """
            {"id":"msg_2","time":{"created":1},"type":"user","text":"look",
             "files":[{"data":"$data","mime":"image/png","source":{"type":"inline"},"name":"shot.png"}]}
        """.trimIndent()
        val message = OpenCodeJson.decodeFromString(SessionMessageInfo.serializer(), wire)

        val user = MessageCodec.decode(MessageCodec.encode(message).json)?.toDomain() as Message.User

        assertEquals(listOf(ImageAttachment("shot.png", "image/png", null)), user.images)
        assertEquals("data:image/png;base64,$data", (message.toDomain() as Message.User).images.single().dataUri)
    }

    @Test
    fun `short strings, numbers and nulls are untouched`() {
        assertEquals(JsonPrimitive("hello"), MessageCodec.slim(JsonPrimitive("hello")))
        assertEquals(JsonPrimitive(3), MessageCodec.slim(JsonPrimitive(3)))
    }
}
