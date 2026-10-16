package io.github.beppe2880.opencodemobile.core.data.server

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.beppe2880.opencodemobile.core.domain.CredentialKind
import io.github.beppe2880.opencodemobile.core.domain.ServerProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.IOException
import java.security.GeneralSecurityException

internal data class StoredServers(val profiles: List<ServerProfile> = emptyList(), val activeId: String? = null)

/** Persistence for saved servers, including their secrets. Calls may block. */
internal interface ServerStore {
    fun load(): StoredServers
    fun save(servers: StoredServers)
}

/**
 * Keeps profiles in EncryptedSharedPreferences backed by an Android Keystore key. When the file
 * cannot be decrypted (for example after a backup restore to a new device) it is reset, so the
 * user pairs again instead of the app failing to start.
 */
internal class EncryptedServerStore(private val context: Context) : ServerStore {

    private val prefs: SharedPreferences by lazy {
        try {
            open()
        } catch (e: GeneralSecurityException) {
            reset()
        } catch (e: IOException) {
            reset()
        }
    }

    override fun load(): StoredServers = StoredServers(
        profiles = ServerProfilesCodec.decode(prefs.getString(KEY_PROFILES, null)),
        activeId = prefs.getString(KEY_ACTIVE, null),
    )

    override fun save(servers: StoredServers) {
        prefs.edit()
            .putString(KEY_PROFILES, ServerProfilesCodec.encode(servers.profiles))
            .putString(KEY_ACTIVE, servers.activeId)
            .commit()
    }

    private fun open(): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private fun reset(): SharedPreferences {
        context.deleteSharedPreferences(FILE_NAME)
        return open()
    }

    private companion object {
        const val FILE_NAME = "opencode_servers"
        const val KEY_PROFILES = "profiles"
        const val KEY_ACTIVE = "active_id"
    }
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
