package io.github.beppe2880.opencodemobile.core.data.server

import io.github.beppe2880.opencodemobile.core.domain.CredentialKind
import io.github.beppe2880.opencodemobile.core.domain.ServerProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal data class StoredServers(val profiles: List<ServerProfile> = emptyList(), val activeId: String? = null)

/** Persistence for saved servers, including their secrets. Calls may block. */
internal interface ServerStore {
    fun load(): StoredServers
    fun save(servers: StoredServers)
}

/** Persisted JSON schema of the profile list. Unreadable data yields an empty list. */
internal object ServerProfilesCodec {

    @Serializable
    private data class StoredProfile(
        val id: String,
        val name: String,
        val baseUrl: String,
        val username: String,
        val secret: String,
        val credential: String,
        val addedAt: Long,
    )

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(StoredProfile.serializer())

    fun encode(profiles: List<ServerProfile>): String = json.encodeToString(
        serializer,
        profiles.map { StoredProfile(it.id, it.name, it.baseUrl, it.username, it.secret, it.credential.name, it.addedAt) },
    )

    fun decode(raw: String?): List<ServerProfile> {
        if (raw.isNullOrBlank()) return emptyList()
        val stored = try {
            json.decodeFromString(serializer, raw)
        } catch (_: IllegalArgumentException) {
            return emptyList()
        }
        return stored.mapNotNull { profile ->
            val credential = CredentialKind.entries.firstOrNull { it.name == profile.credential } ?: return@mapNotNull null
            ServerProfile(profile.id, profile.name, profile.baseUrl, profile.username, profile.secret, credential, profile.addedAt)
        }
    }
}
