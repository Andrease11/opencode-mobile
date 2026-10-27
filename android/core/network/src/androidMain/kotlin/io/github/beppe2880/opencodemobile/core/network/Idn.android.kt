package io.github.beppe2880.opencodemobile.core.network

import java.net.IDN

internal actual fun idnToAscii(host: String): String? = try {
    IDN.toASCII(host)
} catch (_: IllegalArgumentException) {
    null
}
