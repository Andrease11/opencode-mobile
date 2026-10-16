package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.core.domain.ConnectTarget
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** Parses scanned QR codes, pasted links and typed hosts into a [ConnectTarget]. */
object ConnectLinkParser {

    const val DEFAULT_PORT = 4096

    fun parse(raw: String): ConnectTarget? {
        val text = raw.trim()
        if (text.startsWith("{")) return parsePairQr(text)
        if (text.isEmpty() || text.any { it.isWhitespace() }) return null
        val withScheme = if ("://" in text) text else "http://$text"
        val url = withScheme.toHttpUrlOrNull() ?: return null
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
        val host = baseUrl.toHttpUrlOrNull()?.host ?: return false
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
