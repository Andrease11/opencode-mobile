package io.github.beppe2880.opencodemobile.core.domain

/** A saved OpenCode server. [secret] is either the server password or a paired session token. */
data class ServerProfile(
    val id: String,
    val name: String,
    val baseUrl: String,
    val username: String = DEFAULT_USERNAME,
    val secret: String,
    val credential: CredentialKind,
    val addedAt: Long,
) {
    companion object {
        const val DEFAULT_USERNAME = "opencode"
    }
}

enum class CredentialKind { Password, PairingToken }

data class ServerInfo(
    val version: String,
    val urls: List<String>,
)

/** Result of parsing anything the user can scan or paste to connect. */
sealed interface ConnectTarget {
    /**
     * A pairing code for a server. Links (`http(s)://host:port/auth/connect/<code>`) carry one
     * address; the `opencode pair` QR code lists every address of the server, so the ones after
     * [baseUrl] are in [otherUrls] and the client picks the first reachable one.
     */
    data class Pairing(
        val baseUrl: String,
        val code: String,
        val otherUrls: List<String> = emptyList(),
    ) : ConnectTarget {
        val urls: List<String> get() = listOf(baseUrl) + otherUrls
    }

    /** A bare server URL that still needs a password or pairing code. */
    data class Url(val baseUrl: String) : ConnectTarget
}
