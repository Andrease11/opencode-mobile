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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Authenticated access to one OpenCode server. Create through [OpenCodeClientFactory]. */
class OpenCodeClient internal constructor(
    val baseUrl: String,
    private val http: OkHttpClient,
    retrofit: Retrofit,
) {
    val server: ServerApi = retrofit.create(ServerApi::class.java)
    val sessions: SessionApi = retrofit.create(SessionApi::class.java)
    val permissions: PermissionApi = retrofit.create(PermissionApi::class.java)
    val agents: AgentApi = retrofit.create(AgentApi::class.java)
    val models: ModelApi = retrofit.create(ModelApi::class.java)
    val providers: ProviderApi = retrofit.create(ProviderApi::class.java)
    val projects: ProjectApi = retrofit.create(ProjectApi::class.java)
    val commands: CommandApi = retrofit.create(CommandApi::class.java)
    val files: FilesystemApi = retrofit.create(FilesystemApi::class.java)

    private val streamHttp: OkHttpClient by lazy { streamClient(STREAM_READ_TIMEOUT_MILLIS) }

    private fun streamClient(readTimeoutMillis: Long): OkHttpClient =
        http.newBuilder().readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS).build()

    /**
     * Cold stream of `/api/event`. Each collection opens one HTTP call that is cancelled with
     * the collector. Unknown event types are skipped. Completes when the server closes the
     * stream and fails with [OpenCodeError] on HTTP or transport errors.
     *
     * The server sends a heartbeat comment every 15 s, so a read that stalls for
     * [readTimeoutMillis] means a dead connection and fails with [OpenCodeError.Unreachable].
     */
    fun events(readTimeoutMillis: Long = STREAM_READ_TIMEOUT_MILLIS): Flow<OpenCodeEvent> = channelFlow {
        val client = if (readTimeoutMillis == STREAM_READ_TIMEOUT_MILLIS) streamHttp else streamClient(readTimeoutMillis)
        val call = client.newCall(
            Request.Builder().url(baseUrl.trimEnd('/') + "/api/event").header("Accept", "text/event-stream").build(),
        )
        // Reads block, so cancellation has to cancel the call to unblock the reader.
        val reading = launch(Dispatchers.IO) {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) throw httpError(response.code, response.body?.readBounded())
                    val reader = SseReader(response.body!!.source())
                    while (true) {
                        val payload = reader.next() ?: break
                        decodeEvent(payload)?.let { send(it) }
                    }
                }
            } catch (e: IOException) {
                if (isActive) throw e.toOpenCodeError()
            }
        }
        try {
            reading.join()
        } finally {
            call.cancel()
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
