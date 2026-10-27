package io.github.beppe2880.opencodemobile.core.data.draft

import com.russhwolf.settings.Settings
import io.github.beppe2880.opencodemobile.core.data.OpenCodeClients
import io.github.beppe2880.opencodemobile.core.domain.DraftRepository

/**
 * Drafts in app-private preferences, keyed by saved server and session. They contain what
 * the user is typing, so they are never logged.
 */
internal class PreferencesDraftRepository(
    private val prefs: Settings,
    private val clients: OpenCodeClients,
) : DraftRepository {

    override fun load(sessionId: String): String = key(sessionId)?.let { prefs.getStringOrNull(it) }.orEmpty()

    override fun save(sessionId: String, text: String) {
        val key = key(sessionId) ?: return
        if (text.isBlank()) prefs.remove(key) else prefs.putString(key, text)
    }

    private fun key(sessionId: String): String? = clients.activeIdOrNull()?.let { key(it, sessionId) }

    companion object {
        private fun key(serverId: String, sessionId: String) = "$serverId/$sessionId"

        /** Deletes every draft kept for [serverId], e.g. when the server is removed. */
        fun clearServer(prefs: Settings, serverId: String) {
            val prefix = key(serverId, "")
            prefs.keys.filter { it.startsWith(prefix) }.forEach(prefs::remove)
        }
    }
}
