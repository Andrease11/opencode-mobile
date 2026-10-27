package io.github.beppe2880.opencodemobile

import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSURL

/** Requests from `opencodemobile://` links (the home screen widget), cleared once handled. */
internal object AppLinks {
    val sessionToOpen = MutableStateFlow<String?>(null)
    val newSession = MutableStateFlow(false)
}

/**
 * Handles a link opened by iosApp: `opencodemobile://session/<id>` opens a chat,
 * `opencodemobile://new-session` the new session sheet. Anything else is ignored.
 */
@Suppress("unused")
fun openLink(url: String) {
    val link = NSURL.URLWithString(url) ?: return
    if (link.scheme != LINK_SCHEME) return
    when (link.host) {
        "session" -> link.pathComponents?.getOrNull(1)?.let { it as? String }?.takeIf(::isSessionId)?.let { AppLinks.sessionToOpen.value = it }
        "new-session" -> AppLinks.newSession.value = true
    }
}

private const val LINK_SCHEME = "opencodemobile"
