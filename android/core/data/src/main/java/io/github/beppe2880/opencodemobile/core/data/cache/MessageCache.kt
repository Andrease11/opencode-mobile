package io.github.beppe2880.opencodemobile.core.data.cache

/** One stored transcript entry: the server message id and its wire JSON. */
internal data class CachedEntry(val id: String, val json: String)

/**
 * The stored part of a transcript: the newest [entries], oldest first, and the server cursor
 * to the history before them ([olderCursor], null when the history is complete or when older
 * entries were dropped at the cap; the repository then reads older history without the cache).
 */
internal data class CachedTranscript(val entries: List<CachedEntry> = emptyList(), val olderCursor: String? = null)

/** Local copy of session transcripts, scoped by saved server. */
internal interface MessageCache {
    /** Stored transcript of [sessionId] on [serverId]; empty when never synced. */
    suspend fun read(serverId: String, sessionId: String): CachedTranscript

    /**
     * Keeps the entries up to and including [keepThrough] (none when null), appends [newer]
     * after them and stores [olderCursor]. Beyond [max] entries the oldest are dropped and the
     * cursor is cleared, so the history ends there.
     */
    suspend fun write(
        serverId: String,
        sessionId: String,
        keepThrough: String?,
        newer: List<CachedEntry>,
        olderCursor: String?,
        max: Int,
    )

    /** Adds [older] (oldest first) before the stored entries, within [max] like [write]. */
    suspend fun prepend(serverId: String, sessionId: String, older: List<CachedEntry>, olderCursor: String?, max: Int)

    /** Forgets every transcript of [serverId], e.g. when the server is removed. */
    suspend fun clearServer(serverId: String)
}
