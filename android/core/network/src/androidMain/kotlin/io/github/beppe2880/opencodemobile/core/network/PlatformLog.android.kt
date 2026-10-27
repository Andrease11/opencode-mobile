package io.github.beppe2880.opencodemobile.core.network

import android.util.Log

internal actual fun logWarning(tag: String, message: String) {
    // android.util.Log is a stub in JVM unit tests.
    runCatching { Log.w(tag, message) }
}

internal actual fun logDebug(tag: String, message: String) {
    runCatching { Log.d(tag, message) }
}
