package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.api.apis.ServerApi
import io.github.beppe2880.opencodemobile.core.domain.OpenCodeError
import io.github.beppe2880.opencodemobile.core.domain.ServerProfile
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.utils.EmptyContent
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlin.io.encoding.Base64

/** HTTP Basic credentials. [toString] never reveals the secret. */
class BasicCredentials(val username: String = ServerProfile.DEFAULT_USERNAME, val password: String) {
    internal val header: String get() = "Basic " + Base64.encode("$username:$password".encodeToByteArray())
    override fun toString(): String = "BasicCredentials(username=$username)"
}

/**
 * Builds [OpenCodeClient]s that share one HTTP engine and its connection pool. With
 * [debugLogging] only request lines and headers are logged, with `Authorization` redacted;
 * bodies (prompts, tokens) never are.
 */
class OpenCodeClientFactory(private val debugLogging: Boolean = false) {

    private val baseHttp: HttpClient = HttpClient {
        expectSuccess = false
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
        // Encodes request bodies and sends `Accept: application/json`, which /auth/connect needs
        // to answer with JSON. Responses are decoded by [awaitBody].
        install(ContentNegotiation) { json(OpenCodeJson) }
        install(NoContentTypeWithoutBody)
    }

    fun create(baseUrl: String, credentials: BasicCredentials?): OpenCodeClient {
        val http = baseHttp.config {
            if (credentials != null) defaultRequest { header(HttpHeaders.Authorization, credentials.header) }
            if (debugLogging) {
                install(Logging) {
                    level = LogLevel.HEADERS
                    logger = object : Logger {
                        override fun log(message: String) = logDebug("OpenCodeHttp", message)
                    }
                    sanitizeHeader { it == HttpHeaders.Authorization || it == HttpHeaders.SetCookie }
                }
            }
        }
        return OpenCodeClient(baseUrl, http)
    }

    /** Exchanges a single-use pairing [code] for a session token. Never logged: the URL holds the code. */
    suspend fun redeemPairing(baseUrl: String, code: String): String = try {
        awaitBody { ServerApi(baseUrl, baseHttp).serverConnect(code) }.token
    } catch (e: OpenCodeError) {
        throw pairingError(e)
    }
}

/**
 * The generated client marks every POST/PUT/PATCH as JSON, also without a body (interrupt,
 * revert commit, pair); send those without `Content-Type`, as before.
 */
private val NoContentTypeWithoutBody = createClientPlugin("NoContentTypeWithoutBody") {
    onRequest { request, _ ->
        if (request.body is EmptyContent) request.headers.remove(HttpHeaders.ContentType)
    }
}
