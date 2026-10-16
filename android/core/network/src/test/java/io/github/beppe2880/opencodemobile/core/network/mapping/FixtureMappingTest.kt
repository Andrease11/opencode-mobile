package io.github.beppe2880.opencodemobile.core.network.mapping

import io.github.beppe2880.opencodemobile.api.models.AgentList200Response
import io.github.beppe2880.opencodemobile.api.models.ModelList200Response
import io.github.beppe2880.opencodemobile.api.models.Project
import io.github.beppe2880.opencodemobile.api.models.SessionCreate200Response
import io.github.beppe2880.opencodemobile.api.models.SessionMessagesResponse
import io.github.beppe2880.opencodemobile.api.models.SessionsResponse
import io.github.beppe2880.opencodemobile.core.domain.AgentMode
import io.github.beppe2880.opencodemobile.core.domain.Message
import io.github.beppe2880.opencodemobile.core.domain.ModelRef
import io.github.beppe2880.opencodemobile.core.domain.Part
import io.github.beppe2880.opencodemobile.core.domain.SessionOutcome
import io.github.beppe2880.opencodemobile.core.domain.TokenUsage
import io.github.beppe2880.opencodemobile.core.domain.ToolStatus
import io.github.beppe2880.opencodemobile.core.network.Fixtures
import io.github.beppe2880.opencodemobile.core.network.MessageCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FixtureMappingTest {

    @Test
    fun `session get maps every field`() {
        val session = Fixtures.decode<SessionCreate200Response>("session_get.json").data.toDomain()

        assertEquals("ses_ec9cb5c70ffe3M22jWcPWdSAIF", session.id)
        assertNull(session.parentId)
        assertEquals("event probe", session.title)
        assertEquals("/home/user/project", session.directory)
        assertEquals("project", session.directoryName)
        assertEquals("build", session.agent)
        // The fixture's "default" variant means none was chosen.
        assertEquals(ModelRef("claude-haiku-5-5", "anthropic"), session.model)
        assertEquals(SessionOutcome.Succeeded, session.outcome)
        assertEquals(TokenUsage(input = 6, output = 354, reasoning = 0, cacheRead = 14233, cacheWrite = 18603), session.tokens)
        assertEquals(1791910781843, session.createdAt)
        assertEquals(1791910789784, session.updatedAt)
        assertNull(session.archivedAt)
    }

    @Test
    fun `session list and projects decode`() {
        val sessions = Fixtures.decode<SessionsResponse>("session_list.json")
        assertEquals(listOf("ses_ec9cb5c70ffe3M22jWcPWdSAIF"), sessions.data.map { it.toDomain().id })
        assertTrue(sessions.cursor.next != null)

        val project = Fixtures.decode<List<Project>>("project_list.json").single().toDomain()
        assertEquals("/home/user/project", project.directory)
        assertEquals("project", project.displayName)
        assertEquals(1791910781843, project.updatedAt)
    }

    @Test
    fun `transcript drops system entries and keeps the idle marker closing the execution`() {
        val messages = Fixtures.decode<SessionMessagesResponse>("session_messages.json").data.mapNotNull { it.toDomain() }

        assertEquals(
            listOf(Message.User::class, Message.Assistant::class, Message.Assistant::class, Message.Idle::class),
            messages.map { it::class },
        )
        val user = messages[0] as Message.User
        assertTrue(user.text.startsWith("Read the file a.txt"))
        assertFalse(user.pending)
    }

    @Test
    fun `cached wire json round-trips to the same transcript`() {
        val wire = Fixtures.decode<SessionMessagesResponse>("session_messages.json").data
        val encoded = wire.map(MessageCodec::encode)

        assertEquals(wire.map { it.toDomain() }, encoded.map { MessageCodec.decode(it.json)?.toDomain() })
        assertTrue(encoded.all { it.id.startsWith("msg_") })
        assertTrue(wire.all(MessageCodec::isSettled))
        assertNull(MessageCodec.decode("""{"type":"from-the-future"}"""))
    }

    @Test
    fun `assistant content maps to typed parts`() {
        val messages = Fixtures.decode<SessionMessagesResponse>("session_messages.json").data.mapNotNull { it.toDomain() }
        val tools = messages[1] as Message.Assistant
        val answer = messages[2] as Message.Assistant

        assertEquals("build", tools.agent)
        assertEquals("anthropic/claude-haiku-5-5", tools.model.key)
        assertEquals("tool-calls", tools.finish)
        assertEquals(1791910788337, tools.completedAt)
        assertFalse(tools.isStreaming)

        val reasoning = tools.parts[0] as Part.Reasoning
        assertEquals(0, reasoning.ordinal)
        assertFalse(reasoning.streaming)
        assertEquals(1791910788046, reasoning.completedAt)

        val read = tools.parts[1] as Part.Tool
        assertEquals("toolu_01FxbsWdoDPjLcZNVYRnNMVg", read.callId)
        assertEquals("read", read.name)
        assertEquals(ToolStatus.Completed, read.status)
        assertEquals("""{"path":"a.txt"}""", read.input)
        assertEquals("Read file a.txt, lines 1-1\n1: hello", read.output)
        assertEquals(mapOf("truncated" to "false"), read.metadata)

        val shell = tools.parts[2] as Part.Tool
        assertEquals("shell", shell.name)
        assertEquals("""{"command":"echo probe-ok"}""", shell.input)
        assertEquals("probe-ok\n", shell.output)
        assertEquals("0", shell.metadata["exit"])

        assertEquals(listOf("reasoning-0", "text-0"), answer.parts.map { it.key })
        assertEquals(
            "Read `a.txt` (it contains \"hello\") and ran `echo probe-ok`, which printed `probe-ok`.",
            (answer.parts[1] as Part.Text).text,
        )
    }

    @Test
    fun `agents keep hidden and subagent flags`() {
        val agents = Fixtures.decode<AgentList200Response>("agent_list.json").data.map { it.toDomain() }
        val build = agents.first { it.id == "build" }

        assertEquals("Build", build.name)
        assertEquals(AgentMode.Primary, build.mode)
        assertFalse(build.hidden)
        assertTrue(agents.first { it.id == "explore" }.hidden)
        assertEquals(AgentMode.Subagent, agents.first { it.id == "general" }.mode)
    }

    @Test
    fun `models expose ref, limits and variants`() {
        val models = Fixtures.decode<ModelList200Response>("model_list.json").data.map { it.toDomain() }
        val haiku = models.first { it.ref.id == "claude-haiku-5-5" }

        assertEquals(ModelRef("claude-haiku-5-5", "anthropic"), haiku.ref)
        assertEquals("Claude Haiku 5.5", haiku.name)
        assertEquals(1_000_000, haiku.contextLimit)
        assertTrue(haiku.supportsImages)
        assertEquals(listOf("none", "low", "medium", "high", "xhigh", "max"), haiku.variants)
        assertEquals(1791331200000, haiku.releasedAt)
        assertEquals("active", haiku.status)
    }
}
