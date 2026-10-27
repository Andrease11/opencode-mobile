package io.github.beppe2880.opencodemobile

private val SESSION_ID = Regex("ses_[A-Za-z0-9]{1,64}")

/** Links from outside the app (notifications, widgets) are untrusted: accept only a well-formed session id. */
fun isSessionId(id: String): Boolean = SESSION_ID.matches(id)
