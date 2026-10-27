package io.github.beppe2880.opencodemobile.core.data.server

import com.russhwolf.settings.KeychainSettings

/**
 * Keeps profiles, secrets included, in the iOS Keychain of this app. Items are readable only
 * while the device is unlocked and are not restored to other devices.
 */
internal class KeychainServerStore(
    private val keychain: KeychainSettings = KeychainSettings(service = SERVICE),
) : ServerStore {

    override fun load(): StoredServers = StoredServers(
        profiles = ServerProfilesCodec.decode(keychain.getStringOrNull(KEY_PROFILES)),
        activeId = keychain.getStringOrNull(KEY_ACTIVE),
    )

    override fun save(servers: StoredServers) {
        keychain.putString(KEY_PROFILES, ServerProfilesCodec.encode(servers.profiles))
        val active = servers.activeId
        if (active == null) keychain.remove(KEY_ACTIVE) else keychain.putString(KEY_ACTIVE, active)
    }

    private companion object {
        const val SERVICE = "io.github.beppe2880.opencodemobile.servers"
        const val KEY_PROFILES = "profiles"
        const val KEY_ACTIVE = "active_id"
    }
}
