package io.github.beppe2880.opencodemobile.core.network

import platform.Foundation.NSLog

internal actual fun logWarning(tag: String, message: String) {
    NSLog("%s: %s", tag, message)
}

internal actual fun logDebug(tag: String, message: String) {
    NSLog("%s: %s", tag, message)
}
