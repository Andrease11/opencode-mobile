package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.api.models.OpenCodeEvent
import okio.Buffer

/** Sanitized responses recorded from OpenCode 2.0.27, shared through core:testing resources. */
internal object Fixtures {
    val json = OpenCodeJson

    fun text(name: String): String =
        requireNotNull(javaClass.classLoader?.getResource(name)) { "Missing fixture $name" }.readText()

    inline fun <reified T> decode(name: String): T = json.decodeFromString(text(name))

    /** Every payload of `events.sse`, in order, including types this client cannot decode. */
    fun ssePayloads(): List<String> {
        val reader = SseReader(Buffer().writeUtf8(text("events.sse")))
        return generateSequence { reader.next() }.toList()
    }

    fun events(): List<OpenCodeEvent> = ssePayloads().mapNotNull(::decodeEvent)
}
