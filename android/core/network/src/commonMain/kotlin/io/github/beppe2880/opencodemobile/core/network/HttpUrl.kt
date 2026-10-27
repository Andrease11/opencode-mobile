package io.github.beppe2880.opencodemobile.core.network

/**
 * The parts of an `http`/`https` URL that [ConnectLinkParser] needs, parsed like OkHttp's
 * `HttpUrl` did before the client became multiplatform: hosts are lowercased, percent-decoded
 * and converted to ASCII (IDN); IPv6 hosts are canonical (RFC 5952) and returned without
 * brackets, IPv4-mapped ones as IPv4; `\` separates path segments like `/`; [port] is the
 * scheme default when the URL has none.
 */
internal data class HttpUrl(val scheme: String, val host: String, val port: Int, val pathSegments: List<String>) {
    companion object {
        private val URL = Regex("""^([A-Za-z][A-Za-z0-9+.-]*)://([^/\\?#]*)([^?#]*)(?:[?#].*)?$""")
        private val LABEL = Regex("""^[a-z0-9_-]+$""")

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
                canonicalIpv6(authority.substring(1, end)) to rest.removePrefix(":")
            } else {
                if (authority.count { it == ':' } > 1) return null
                canonicalHost(authority.substringBefore(':')) to
                    (if (':' in authority) authority.substringAfter(':') else "")
            }
            if (host.isNullOrEmpty()) return null
            val port = when {
                portText.isEmpty() -> if (scheme == "https") 443 else 80
                portText.removePrefix("+").let { it.isNotEmpty() && it.all(Char::isDigit) } ->
                    portText.removePrefix("+").toIntOrNull()?.takeIf { it in 1..65535 } ?: return null
                else -> return null
            }
            val segments = match.groupValues[3].replace('\\', '/').split('/').drop(1).map(::percentDecode)
            return HttpUrl(scheme, host, port, segments)
        }

        private fun canonicalHost(raw: String): String? {
            val decoded = percentDecode(raw).trimEnd('.').ifEmpty { return null }
            val ascii = (if (decoded.all { it.code < 0x80 }) decoded else idnToAscii(decoded) ?: return null).lowercase()
            return ascii.takeIf { host -> host.split('.').all { it.isNotEmpty() && LABEL.matches(it) } }
        }

        /** RFC 5952 text of an IPv6 literal; IPv4-mapped addresses become IPv4. Zone ids are rejected. */
        internal fun canonicalIpv6(raw: String): String? {
            if (':' !in raw || '%' in raw) return null
            val groups = ipv6Groups(raw.lowercase()) ?: return null
            if (groups.take(5).all { it == 0 } && groups[5] == 0xFFFF) {
                return "${groups[6] shr 8}.${groups[6] and 0xFF}.${groups[7] shr 8}.${groups[7] and 0xFF}"
            }
            // Longest run of at least two zero groups, the first one on ties.
            var bestStart = -1
            var bestLength = 1
            var i = 0
            while (i < 8) {
                if (groups[i] != 0) { i++; continue }
                val start = i
                while (i < 8 && groups[i] == 0) i++
                if (i - start > bestLength) {
                    bestStart = start
                    bestLength = i - start
                }
            }
            if (bestStart < 0) return groups.joinToString(":") { it.toString(16) }
            val head = groups.subList(0, bestStart).joinToString(":") { it.toString(16) }
            val tail = groups.subList(bestStart + bestLength, 8).joinToString(":") { it.toString(16) }
            return "$head::$tail"
        }

        private fun ipv6Groups(text: String): List<Int>? {
            val halves = text.split("::")
            if (halves.size > 2) return null
            fun parts(half: String): List<Int>? {
                if (half.isEmpty()) return emptyList()
                val pieces = half.split(':')
                val out = mutableListOf<Int>()
                pieces.forEachIndexed { index, piece ->
                    if (index == pieces.lastIndex && '.' in piece) {
                        val octets = piece.split('.').map { it.toIntOrNull()?.takeIf { o -> o in 0..255 } ?: return null }
                        if (octets.size != 4) return null
                        out += (octets[0] shl 8) or octets[1]
                        out += (octets[2] shl 8) or octets[3]
                    } else {
                        if (piece.isEmpty() || piece.length > 4) return null
                        out += piece.toIntOrNull(16) ?: return null
                    }
                }
                return out
            }
            val head = parts(halves[0]) ?: return null
            if (halves.size == 1) return head.takeIf { it.size == 8 }
            val tail = parts(halves[1]) ?: return null
            val missing = 8 - head.size - tail.size
            if (missing < 1) return null
            return head + List(missing) { 0 } + tail
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

/** ASCII (punycode) form of an internationalized host name; null when it is not valid. */
internal expect fun idnToAscii(host: String): String?
