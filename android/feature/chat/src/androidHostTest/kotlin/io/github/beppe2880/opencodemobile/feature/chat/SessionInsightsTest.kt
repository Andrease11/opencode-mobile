package io.github.beppe2880.opencodemobile.feature.chat

import io.github.beppe2880.opencodemobile.core.domain.Message
import io.github.beppe2880.opencodemobile.core.domain.ModelInfo
import io.github.beppe2880.opencodemobile.core.domain.ModelRef
import io.github.beppe2880.opencodemobile.core.domain.NoticeKind
import io.github.beppe2880.opencodemobile.core.domain.Part
import io.github.beppe2880.opencodemobile.core.domain.TokenUsage
import io.github.beppe2880.opencodemobile.core.domain.ToolStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionInsightsTest {
    private val model = ModelRef("haiku", "anthropic")
    private val models = listOf(ModelInfo(model, "Haiku", "anthropic", null, 200_000, true, emptyList(), 0, true, "active"))

    private fun step(id: String, tokens: TokenUsage, parts: List<Part> = emptyList()) =
        Message.Assistant(id, 1, "build", model, parts, completedAt = 2, tokens = tokens)

    @Test
    fun `context is the last step with tokens, measured against the model window`() {
        val messages = listOf(
            step("a1", TokenUsage(input = 10, cacheRead = 90_000)),
            step("a2", TokenUsage(input = 5, output = 1_000, cacheRead = 99_000, cacheWrite = 0)),
            step("a3", TokenUsage()),
        )

        val usage = SessionInsights.context(messages, models)!!
        assertEquals(100_005L, usage.tokens)
        assertEquals(50, usage.percent)
        assertNull(SessionInsights.context(messages, emptyList())!!.percent)
    }

    @Test
    fun `a completed compaction resets the context until the next step`() {
        val before = step("a1", TokenUsage(cacheRead = 150_000))
        val compacted = Message.Notice("c1", 3, NoticeKind.Compaction, "summary")

        assertNull(SessionInsights.context(listOf(before, compacted), models))
        val running = Message.Notice("c1", 3, NoticeKind.Compacting, "")
        assertEquals(150_000L, SessionInsights.context(listOf(before, running), models)!!.tokens)
        assertEquals(2_000L, SessionInsights.context(listOf(before, compacted, step("a2", TokenUsage(input = 2_000))), models)!!.tokens)
    }

    @Test
    fun `subagent tools link to their child session`() {
        val subagent = Part.Tool("c2", "subagent", ToolStatus.Running, "{}", metadata = mapOf("sessionID" to "ses_v2"))
        assertEquals("ses_v2", SessionInsights.childSession(subagent))
        val task = Part.Tool("c1", "task", ToolStatus.Running, "{}", metadata = mapOf("sessionId" to "ses_child"))
        assertEquals("ses_child", SessionInsights.childSession(task))
        assertNull(SessionInsights.childSession(task.copy(name = "read")))
        assertNull(SessionInsights.childSession(task.copy(metadata = emptyMap())))
    }
}
