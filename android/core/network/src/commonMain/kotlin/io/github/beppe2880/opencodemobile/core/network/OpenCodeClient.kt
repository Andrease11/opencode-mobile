package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.api.apis.AgentApi
import io.github.beppe2880.opencodemobile.api.apis.CommandApi
import io.github.beppe2880.opencodemobile.api.apis.FilesystemApi
import io.github.beppe2880.opencodemobile.api.apis.ModelApi
import io.github.beppe2880.opencodemobile.api.apis.PermissionApi
import io.github.beppe2880.opencodemobile.api.apis.ProjectApi
import io.github.beppe2880.opencodemobile.api.apis.ProviderApi
import io.github.beppe2880.opencodemobile.api.apis.ServerApi
import io.github.beppe2880.opencodemobile.api.apis.SessionApi
import io.github.beppe2880.opencodemobile.api.models.OpenCodeEvent
import io.github.beppe2880.opencodemobile.api.models.OpenCodeEventSerializer
import io.github.beppe2880.opencodemobile.core.domain.OpenCodeError
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.contentnegotiation.exclude
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.io.IOException

/** Authenticated access to one OpenCode server. Create through [OpenCodeClientFactory]. */
class OpenCodeClient internal constructor(
    val baseUrl: String,
    private val http: HttpClient,
) {
    val server: ServerApi = ServerApi(baseUrl, http)
    val sessions: SessionApi = SessionApi(baseUrl, http)
    val permissions: PermissionApi = PermissionApi(baseUrl, http)
    val agents: AgentApi = AgentApi(baseUrl, http)
    val models: ModelApi = ModelApi(baseUrl, http)
    val providers: ProviderApi = ProviderApi(baseUrl, http)
    val projects: ProjectApi = ProjectApi(baseUrl, http)
    val commands: CommandApi = CommandApi(baseUrl, http)
    val files: FilesystemApi = FilesystemApi(baseUrl, http)

    /**
     * Cold stream of `/api/event`. Each collection opens one HTTP call that is cancelled with
     * the collector. Unknown event types are skipped. Completes when the server closes the
     * stream and fails with [OpenCodeError] on HTTP or transport errors.
     *
     * The server sends a heartbeat comment every 15 s, so a read that stalls for
     * [readTimeoutMillis] means a dead connection and fails with [OpenCodeError.Unreachable].
     */
    fun events(readTimeoutMillis: Long = STREAM_READ_TIMEOUT_MILLIS): Flow<OpenCodeEvent> = channelFlow {
        try {
            http.prepareGet(baseUrl.trimEnd('/') + "/api/event") {
                header(HttpHeaders.Accept, "text/event-stream")
                exclude(ContentType.Application.Json)
                timeout {
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                    socketTimeoutMillis = readTimeoutMillis
                }
            }.execute { response ->
                if (!response.status.isSuccess()) throw httpError(response.status.value, response.readBounded())
                val reader = SseReader(response.bodyAsChannel())
                while (true) {
                    val payload = reader.next() ?: break
                    // `send`, not `emit`: engines such as Darwin run this block in another coroutine.
                    decodeEvent(payload)?.let { send(it) }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw e.toOpenCodeError()
        }
    }

    companion object {
        /** Three missed server heartbeats (sent every 15 s). */
        const val STREAM_READ_TIMEOUT_MILLIS: Long = 45_000
    }
}

/** Decodes one SSE payload; returns null for event types this client version does not know. */
internal fun decodeEvent(payload: String): OpenCodeEvent? = try {
    OpenCodeJson.decodeFromString(OpenCodeEventSerializer, payload)
} catch (_: IllegalArgumentException) {
    // Also covers SerializationException: unknown `type` discriminators or malformed payloads.
    null
}
