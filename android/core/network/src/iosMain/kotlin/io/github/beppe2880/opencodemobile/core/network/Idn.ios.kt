package io.github.beppe2880.opencodemobile.core.network

import platform.Foundation.NSURLComponents

/** `NSURLComponents` converts an internationalized host to punycode in its URL string. */
internal actual fun idnToAscii(host: String): String? =
    NSURLComponents().apply { scheme = "http"; this.host = host }.URL?.host?.takeIf { name -> name.all { it.code < 0x80 } }
