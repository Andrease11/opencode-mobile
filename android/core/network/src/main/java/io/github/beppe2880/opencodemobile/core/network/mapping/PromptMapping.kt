package io.github.beppe2880.opencodemobile.core.network.mapping

import io.github.beppe2880.opencodemobile.api.models.PromptInputFileAttachment
import io.github.beppe2880.opencodemobile.api.models.PromptMention
import io.github.beppe2880.opencodemobile.core.domain.PromptAttachment

/**
 * Wire attachments for a prompt with [text]. A mentioned file keeps the position of its
 * `@path` in the text (dropped when the user deleted the mention after picking it).
 */
fun List<PromptAttachment>.toApi(text: String): List<PromptInputFileAttachment>? = mapNotNull { attachment ->
    val mention = attachment.mention?.let { token ->
        val start = text.indexOf(token).takeIf { it >= 0 } ?: return@mapNotNull null
        PromptMention(start = start.toDouble(), end = (start + token.length).toDouble(), text = token)
    }
    PromptInputFileAttachment(uri = attachment.uri, name = attachment.name, mention = mention)
}.takeIf { it.isNotEmpty() }
