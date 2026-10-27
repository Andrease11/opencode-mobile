package io.github.beppe2880.opencodemobile.feature.connect

/**
 * True when [url] uses plain HTTP towards a host that is not on a local network or a typical
 * VPN range: the password or token would then cross the internet unencrypted. Hostnames are not
 * resolved, so names without a local suffix count as public.
 */
internal fun isPublicCleartext(url: String): Boolean {
    val text = url.trim()
    if (text.isEmpty()) return false
    val withScheme = if ("://" in text) text else "http://$text"
    val scheme = withScheme.substringBefore("://")
    if (!scheme.equals("http", ignoreCase = true)) return false
    val host = hostOf(withScheme.substringAfter("://"))?.lowercase() ?: return false
    return !isLocalHost(host)
}

/** The host of `authority/path...`, without user info, port or IPv6 brackets; null when malformed. */
private fun hostOf(rest: String): String? {
    val authority = rest.substringBefore('/').substringBefore('?').substringBefore('#').substringAfterLast('@')
    val host = if (authority.startsWith("[")) {
        if (!authority.contains(']')) return null
        authority.substring(1, authority.indexOf(']'))
    } else {
        authority.substringBefore(':')
    }
    if (host.isEmpty() || host.any { it.isWhitespace() || it in "<>\"^`{|}" }) return null
    return host
}

private val LOCAL_SUFFIXES = listOf(".local", ".lan", ".home.arpa", ".internal", ".ts.net")

private fun isLocalHost(host: String): Boolean {
    if (host == "localhost" || LOCAL_SUFFIXES.any { host.endsWith(it) }) return true
    ipv4(host)?.let { (a, b) ->
        return a == 10 || a == 127 ||
            (a == 172 && b in 16..31) ||
            (a == 192 && b == 168) ||
            (a == 169 && b == 254) ||
            (a == 100 && b in 64..127) // CGNAT, used by Tailscale
    }
    if (':' in host) {
        return host == "::1" || host.startsWith("fe8") || host.startsWith("fe9") ||
            host.startsWith("fea") || host.startsWith("feb") || host.startsWith("fc") || host.startsWith("fd")
    }
    // A single-label name (my-box) only resolves on a local network.
    return '.' !in host
}

private fun ipv4(host: String): Pair<Int, Int>? {
    val parts = host.split('.')
    if (parts.size != 4) return null
    val numbers = parts.map { it.toIntOrNull()?.takeIf { n -> n in 0..255 } ?: return null }
    return numbers[0] to numbers[1]
}
