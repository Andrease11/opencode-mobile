package io.github.beppe2880.opencodemobile.feature.chat

import java.util.concurrent.ConcurrentHashMap

/**
 * Where the user left each chat, for as long as the app runs (one instance per app). The
 * anchor is the key of the first visible row (the oldest one on screen)
 * plus its pixel offset, so the place survives new messages arriving below it. No entry means
 * "at the bottom".
 */
class ScrollMemory {
    data class Anchor(val key: String, val offset: Int)

    private val anchors = ConcurrentHashMap<String, Anchor>()

    fun get(sessionId: String): Anchor? = anchors[sessionId]

    /** Stores [anchor] for [sessionId]; null clears it (the user is at the bottom). */
    fun put(sessionId: String, anchor: Anchor?) {
        if (anchor == null) anchors.remove(sessionId) else anchors[sessionId] = anchor
    }
}
