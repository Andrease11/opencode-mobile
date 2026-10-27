package io.github.beppe2880.opencodemobile.core.network

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine

/**
 * Minimal Server-Sent Events reader. Joins multi-line `data:` fields and emits one
 * payload per blank-line-terminated event. Comments (heartbeats) and other fields are
 * ignored; an event left unterminated at end of stream is discarded, as the spec requires.
 */
internal class SseReader(private val source: ByteReadChannel) {

    /** Returns the next event payload, or null at end of stream. Suspends until one is complete. */
    suspend fun next(): String? {
        val data = StringBuilder()
        var hasData = false
        while (true) {
            val line = source.readLine() ?: return null
            when {
                line.isEmpty() -> if (hasData) return data.toString()
                line == "data" || line.startsWith("data:") -> {
                    if (hasData) data.append('\n')
                    data.append(line.removePrefix("data").removePrefix(":").removePrefix(" "))
                    hasData = true
                }
            }
        }
    }
}
