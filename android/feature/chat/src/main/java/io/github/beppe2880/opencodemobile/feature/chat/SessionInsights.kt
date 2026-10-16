package io.github.beppe2880.opencodemobile.feature.chat

import io.github.beppe2880.opencodemobile.core.domain.Message
import io.github.beppe2880.opencodemobile.core.domain.ModelInfo
import io.github.beppe2880.opencodemobile.core.domain.NoticeKind
import io.github.beppe2880.opencodemobile.core.domain.Part

/** Tokens the model sees on its next step, against its context window when known. */
data class ContextUsage(val tokens: Long, val limit: Long?) {
    val percent: Int? get() = limit?.takeIf { it > 0 }?.let { Math.round(tokens * 100.0 / it).toInt() }
}

internal object SessionInsights {
    private val SUBAGENT_TOOLS = setOf("task", "subagent")

    /**
     * Like the TUI: the last assistant step with tokens after the latest completed compaction.
     * [messages] must already exclude reverted messages.
     */
    fun context(messages: List<Message>, models: List<ModelInfo>): ContextUsage? {
        val compacted = messages.indexOfLast { it is Message.Notice && it.kind == NoticeKind.Compaction }
        val step = messages.subList(compacted + 1, messages.size)
            .lastOrNull { it is Message.Assistant && it.tokens.total > 0 } as? Message.Assistant ?: return null
        val limit = models.firstOrNull { it.ref.key == step.model.key }?.contextLimit?.takeIf { it > 0 }
        return ContextUsage(step.tokens.total, limit)
    }

    /**
     * The child session a subagent tool runs in, once the server has created it. V2's
     * `subagent` reports `sessionID`; V1's `task` (imported sessions) reports `sessionId`.
     */
    fun childSession(tool: Part.Tool): String? {
        if (tool.name !in SUBAGENT_TOOLS) return null
        return (tool.metadata["sessionID"] ?: tool.metadata["sessionId"])?.takeIf { it.startsWith("ses_") }
    }
}
