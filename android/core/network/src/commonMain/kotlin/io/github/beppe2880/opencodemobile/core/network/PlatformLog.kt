package io.github.beppe2880.opencodemobile.core.network

/** Writes a warning to the platform log. Callers never pass secrets, prompts or tool output. */
internal expect fun logWarning(tag: String, message: String)

/** Writes a debug line to the platform log. Callers never pass secrets, prompts or tool output. */
internal expect fun logDebug(tag: String, message: String)
