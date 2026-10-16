package io.github.beppe2880.opencodemobile.core.data.cache

/** [MessageCache] with the same keep/append/prepend/trim semantics as the Room implementation. */
internal class InMemoryMessageCache : MessageCache {
    val sessions = mutableMapOf<Pair<String, String>, CachedTranscript>()

    override suspend fun read(serverId: String, sessionId: String) = sessions[serverId to sessionId] ?: CachedTranscript()

    override suspend fun write(
        serverId: String,
        sessionId: String,
        keepThrough: String?,
        newer: List<CachedEntry>,
        olderCursor: String?,
        max: Int,
    ) {
        val current = read(serverId, sessionId).entries
        val kept = keepThrough?.let { id -> current.indexOfFirst { it.id == id }.takeIf { it >= 0 } }
            ?.let { current.take(it + 1) }
            .orEmpty()
        val all = kept + newer
        sessions[serverId to sessionId] = CachedTranscript(all.takeLast(max), olderCursor.takeIf { all.size <= max })
    }

    override suspend fun prepend(serverId: String, sessionId: String, older: List<CachedEntry>, olderCursor: String?, max: Int) {
        val current = read(serverId, sessionId).entries
        val kept = older.takeLast((max - current.size).coerceAtLeast(0))
        sessions[serverId to sessionId] = CachedTranscript(kept + current, olderCursor.takeIf { kept.size == older.size })
    }

    override suspend fun clearServer(serverId: String) {
        sessions.keys.removeAll { it.first == serverId }
    }
}
