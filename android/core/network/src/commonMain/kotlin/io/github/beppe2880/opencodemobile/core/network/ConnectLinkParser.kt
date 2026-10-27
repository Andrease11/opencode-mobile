package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.core.domain.ConnectTarget
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/** Parses scanned QR codes, pasted links and typed hosts into a [ConnectTarget]. */
object ConnectLinkParser {

    const val DEFAULT_PORT = 4096

    fun parse(raw: String): ConnectTarget? {
        val text = raw.trim()
        if (text.startsWith("{")) return parsePairQr(text)
        if (text.isEmpty() || text.any { it.isWhitespace() }) return null
        val withScheme = if ("://" in text) text else "http://$text"
        val url = HttpUrl.parse(withScheme) ?: return null
        if (url.host.isBlank()) return null

        val segments = url.pathSegments.filter { it.isNotEmpty() }
        val connectIndex = segments.indexOf("connect")
        val base = baseUrl(url, explicitPort = "://" in text || hasExplicitPort(text))
        if (connectIndex > 0 && segments[connectIndex - 1] == "auth") {
            val code = segments.getOrNull(connectIndex + 1)?.takeIf { it.isNotBlank() } ?: return null
            return ConnectTarget.Pairing(base, code)
        }
        return ConnectTarget.Url(base)
    }

    /** Normalizes a server URL to `scheme://host:port` without a trailing slash. */
    fun normalizeBaseUrl(raw: String): String? = when (val target = parse(raw)) {
        is ConnectTarget.Pairing -> target.baseUrl
        is ConnectTarget.Url -> target.baseUrl
        null -> null
    }

    /**
     * The `opencode pair` QR code: `{"code":"…","urls":["http://192.168.1.20:4096", …]}`, with
     * every address of the server. Loopback addresses go last: on a phone they only work on an
     * emulator through `adb reverse`.
     */
    private fun parsePairQr(text: String): ConnectTarget? {
        val json = runCatching { Json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return null
        val code = (json["code"] as? JsonPrimitive)?.takeIf { it.isString }?.content?.trim()
        if (code.isNullOrEmpty()) return null
        val urls = (json["urls"] as? JsonArray).orEmpty()
            .mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content }
            .mapNotNull { (parse(it) as? ConnectTarget.Url)?.baseUrl }
            .distinct()
            .sortedBy { isLoopback(it) }
        if (urls.isEmpty()) return null
        return ConnectTarget.Pairing(urls.first(), code, urls.drop(1))
    }

    private fun isLoopback(baseUrl: String): Boolean {
        val host = HttpUrl.parse(baseUrl)?.host ?: return false
        return host == "localhost" || host.startsWith("127.") || host == "::1"
    }

    private fun hasExplicitPort(text: String): Boolean =
        Regex("""^(\[[^\]]+]|[^/:\[]+):\d+""").containsMatchIn(text)

    private fun baseUrl(url: HttpUrl, explicitPort: Boolean): String {
        val port = if (explicitPort) url.port else DEFAULT_PORT
        val default = (url.scheme == "http" && port == 80) || (url.scheme == "https" && port == 443)
        val host = if (url.host.contains(':')) "[${url.host}]" else url.host
        return if (default) "${url.scheme}://$host" else "${url.scheme}://$host:$port"
    }
}

/**
 * The parts of an `http`/`https` URL the parser needs. Hosts are lowercased and IPv6 hosts
 * are returned without brackets; [port] is the scheme default when the URL has none.
 */
internal data class HttpUrl(val scheme: String, val host: String, val port: Int, val pathSegments: List<String>) {
    companion object {
        private val URL = Regex("""^([A-Za-z][A-Za-z0-9+.-]*)://([^/?#]*)([^?#]*)(?:[?#].*)?$""")
        private val HOST = Regex("""^[A-Za-z0-9_~%-]+(\.[A-Za-z0-9_~%-]+)*\.?$""")
        private val IPV6 = Regex("""^[0-9A-Fa-f:.]+(%[A-Za-z0-9_.~-]+)?$""")

        fun parse(text: String): HttpUrl? {
            val match = URL.matchEntire(text) ?: return null
            val scheme = match.groupValues[1].lowercase()
            if (scheme != "http" && scheme != "https") return null
            val authority = match.groupValues[2].substringAfterLast('@')
            val (host, portText) = if (authority.startsWith("[")) {
                val end = authority.indexOf(']')
                if (end < 0) return null
                val rest = authority.substring(end + 1)
                if (rest.isNotEmpty() && !rest.startsWith(":")) return null
                authority.substring(1, end).takeIf { ':' in it && IPV6.matches(it) } to rest.removePrefix(":")
            } else {
                if (authority.count { it == ':' } > 1) return null
                authority.substringBefore(':').takeIf { HOST.matches(it) } to
                    (if (':' in authority) authority.substringAfter(':') else "")
            }
            if (host.isNullOrEmpty()) return null
            val port = when {
                portText.isEmpty() -> if (scheme == "https") 443 else 80
                portText.all { it.isDigit() } -> portText.toIntOrNull()?.takeIf { it in 1..65535 } ?: return null
                else -> return null
            }
            val segments = match.groupValues[3].split('/').drop(1).map(::percentDecode)
            return HttpUrl(scheme, host.lowercase(), port, segments)
        }

        private fun percentDecode(segment: String): String {
            if ('%' !in segment) return segment
            val bytes = ArrayList<Byte>(segment.length)
            var i = 0
            while (i < segment.length) {
                val c = segment[i]
                val hex = if (c == '%' && i + 2 <= segment.lastIndex) segment.substring(i + 1, i + 3).toIntOrNull(16) else null
                if (hex != null) {
                    bytes += hex.toByte()
                    i += 3
                } else {
                    c.toString().encodeToByteArray().forEach { bytes += it }
                    i++
                }
            }
            return bytes.toByteArray().decodeToString()
        }
    }
}
