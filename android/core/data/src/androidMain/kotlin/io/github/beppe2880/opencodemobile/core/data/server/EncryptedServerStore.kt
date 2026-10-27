package io.github.beppe2880.opencodemobile.core.data.server

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

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
