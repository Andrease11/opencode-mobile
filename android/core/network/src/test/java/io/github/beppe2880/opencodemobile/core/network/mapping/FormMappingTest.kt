package io.github.beppe2880.opencodemobile.core.network.mapping

import io.github.beppe2880.opencodemobile.api.models.FormInfo
import io.github.beppe2880.opencodemobile.api.models.OpenCodeEvent
import io.github.beppe2880.opencodemobile.core.domain.FormAnswer
import io.github.beppe2880.opencodemobile.core.domain.FormCondition
import io.github.beppe2880.opencodemobile.core.domain.FormField
import io.github.beppe2880.opencodemobile.core.domain.FormOption
import io.github.beppe2880.opencodemobile.core.domain.PromptAttachment
import io.github.beppe2880.opencodemobile.core.domain.ServerEvent
import io.github.beppe2880.opencodemobile.core.network.Fixtures
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormMappingTest {

    private val form = """
        {"id":"frm_1","sessionID":"ses_1","title":"Pick a storage","fields":[
          {"key":"choice","type":"string","title":"Storage","required":true,
           "options":[{"value":"room","label":"Room","description":"SQLite"},{"value":"file","label":"File"}],"custom":true},
          {"key":"count","type":"integer","minimum":1,"maximum":5,"default":2,
           "when":[{"key":"choice","op":"eq","value":"room"}]},
          {"key":"tags","type":"multiselect","options":[{"value":"a","label":"A"}],"default":["a"]},
          {"key":"source","type":"string","hidden":true,"default":"mobile"},
          {"key":"login","type":"external","url":"https://example.com/auth"}
        ]}
    """.trimIndent()

    @Test
    fun `fields map to typed inputs and hidden fields become preset answers`() {
        val mapped = Fixtures.json.decodeFromString<FormInfo>(form).toDomain()

        assertEquals("Pick a storage", mapped.title)
        assertEquals(mapOf("source" to FormAnswer.Text("mobile")), mapped.presetAnswers)
        assertEquals(
            FormField.Text(
                key = "choice", title = "Storage", required = true, custom = true,
                options = listOf(FormOption("room", "Room", "SQLite"), FormOption("file", "File")),
            ),
            mapped.fields[0],
        )
        assertEquals(
            FormField.Number(
                key = "count", integer = true, min = 1.0, max = 5.0, default = 2.0,
                conditions = listOf(FormCondition("choice", equals = true, value = FormAnswer.Text("room"))),
            ),
            mapped.fields[1],
        )
        assertEquals(listOf("a"), (mapped.fields[2] as FormField.MultiSelect).default)
        assertEquals("https://example.com/auth", (mapped.fields[3] as FormField.External).url)
    }

    @Test
    fun `answers encode as the JSON the server expects`() {
        assertEquals(JsonPrimitive(3L), FormAnswer.Number(3.0).toJson())
        assertEquals(JsonPrimitive(2.5), FormAnswer.Number(2.5).toJson())
        assertEquals(JsonPrimitive(true), FormAnswer.Bool(true).toJson())
        assertEquals(JsonArray(listOf(JsonPrimitive("a"))), FormAnswer.Choices(listOf("a")).toJson())
    }

    @Test
    fun `mentions keep their position in the prompt and deleted mentions are dropped`() {
        val text = "read @src/A.kt please"
        val wire = listOf(
            PromptAttachment("file:///p/src/A.kt", "src/A.kt", mention = "@src/A.kt"),
            PromptAttachment("file:///p/src/B.kt", "src/B.kt", mention = "@src/B.kt"),
            PromptAttachment("data:image/png;base64,AA==", "shot.png", "image/png"),
        ).toApi(text)!!

        assertEquals(listOf("file:///p/src/A.kt", "data:image/png;base64,AA=="), wire.map { it.uri })
        assertEquals(5.0, wire[0].mention!!.start, 0.0)
        assertEquals(14.0, wire[0].mention!!.end, 0.0)
        assertNull(wire[1].mention)
        assertNull(emptyList<PromptAttachment>().toApi(text))
    }

    @Test
    fun `form and revert events are session scoped`() {
        fun event(json: String) = Fixtures.json.decodeFromString<OpenCodeEvent>(json).toDomain()

        assertEquals(
            ServerEvent.FormsChanged("ses_1"),
            event("""{"id":"evt_1","created":1,"type":"form.created","data":{"form":$form}}"""),
        )
        assertEquals(
            ServerEvent.FormsChanged("ses_1"),
            event("""{"id":"evt_2","created":1,"type":"form.cancelled","data":{"id":"frm_1","sessionID":"ses_1"}}"""),
        )
        assertEquals(
            ServerEvent.RevertChanged("ses_1", "msg_9"),
            event(
                """{"id":"evt_3","created":1,"type":"session.revert.staged","durable":{"aggregateID":"ses_1","seq":1,"version":1},""" +
                    """"data":{"sessionID":"ses_1","revert":{"messageID":"msg_9"}}}""",
            ),
        )
    }
}
