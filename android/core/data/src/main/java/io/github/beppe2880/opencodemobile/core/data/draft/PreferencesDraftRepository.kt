package io.github.beppe2880.opencodemobile.core.data.draft

import android.content.SharedPreferences
import io.github.beppe2880.opencodemobile.core.data.OpenCodeClients
import io.github.beppe2880.opencodemobile.core.domain.DraftRepository

/**
 * Drafts in app-private preferences, keyed by saved server and session. They contain what
 * the user is typing, so they are never logged.
 */
internal class PreferencesDraftRepository(
    private val prefs: SharedPreferences,
    private val clients: OpenCodeClients,
) : DraftRepository {

    override fun load(sessionId: String): String = key(sessionId)?.let { prefs.getString(it, null) }.orEmpty()

    override fun save(sessionId: String, text: String) {
        val key = key(sessionId) ?: return
        prefs.edit().apply { if (text.isBlank()) remove(key) else putString(key, text) }.apply()
    }

    private fun key(sessionId: String): String? = clients.activeIdOrNull()?.let { key(it, sessionId) }

    companion object {
        private fun key(serverId: String, sessionId: String) = "$serverId/$sessionId"

        /** Deletes every draft kept for [serverId], e.g. when the server is removed. */
        fun clearServer(prefs: SharedPreferences, serverId: String) {
            val prefix = key(serverId, "")
            val keys = prefs.all.keys.filter { it.startsWith(prefix) }
            if (keys.isNotEmpty()) prefs.edit().apply { keys.forEach { remove(it) } }.apply()
        }
    }
}
