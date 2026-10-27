package io.github.beppe2880.opencodemobile.core.data.cache

internal class InMemoryListCache : ListCache {
    val lists = mutableMapOf<Pair<String, String>, String>()

    override suspend fun read(serverId: String, key: String): String? = lists[serverId to key]

    override suspend fun write(serverId: String, key: String, json: String) {
        lists[serverId to key] = json
    }

    override suspend fun clearServer(serverId: String) {
        lists.keys.removeAll { it.first == serverId }
    }
}
