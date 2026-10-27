package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.api.infrastructure.HttpResponse
import io.github.beppe2880.opencodemobile.core.domain.OpenCodeError
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private const val MAX_ERROR_BODY = 16 * 1024

/** Awaits a generated API call and returns its body, throwing [OpenCodeError] on any failure. */
suspend fun <T : Any> awaitBody(call: suspend () -> HttpResponse<T>): T {
    val response = awaitResponse(call)
    return try {
        response.body()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw e.toOpenCodeError()
    }
}

/** Awaits a generated API call whose body is irrelevant, throwing [OpenCodeError] on any failure. */
suspend fun <T : Any> awaitSuccess(call: suspend () -> HttpResponse<T>) {
    awaitResponse(call)
}

private suspend fun <T : Any> awaitResponse(call: suspend () -> HttpResponse<T>): HttpResponse<T> {
    val response = try {
        call()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw e.toOpenCodeError()
    }
    if (!response.success) throw httpError(response.status, response.response.readBounded())
    return response
}

internal suspend fun io.ktor.client.statement.HttpResponse.readBounded(): String? =
    runCatching { bodyAsText().take(MAX_ERROR_BODY) }.getOrNull()

/** Maps transport and decoding failures; [OpenCodeError]s pass through unchanged. */
fun Throwable.toOpenCodeError(): OpenCodeError = when (this) {
    is OpenCodeError -> this
    is IOException -> OpenCodeError.Unreachable(message ?: this::class.simpleName.orEmpty(), this)
    is SerializationException ->
        OpenCodeError.Server(status = 0, tag = null, message = "Unexpected server response: $message")
    else -> OpenCodeError.Server(status = 0, tag = null, message = message ?: this::class.simpleName.orEmpty())
}

/** True when [OpenCodeError.Unreachable] comes from a read or request timeout rather than a lost connection. */
val OpenCodeError.isTimeout: Boolean
    get() = this is OpenCodeError.Unreachable && (cause is SocketTimeoutException || cause is HttpRequestTimeoutException)

/** Maps a non-2xx response with an optional `{ "_tag", "message" }` JSON body. */
internal fun httpError(status: Int, body: String?): OpenCodeError {
    val (tag, message) = parseErrorBody(body)
    return when (status) {
        401 -> OpenCodeError.Unauthorized(message ?: "Authentication failed")
        404 -> OpenCodeError.NotFound(message ?: "Not found")
        else -> OpenCodeError.Server(status, tag, message ?: "HTTP $status")
    }
}

internal fun pairingError(error: OpenCodeError): OpenCodeError = when (error) {
    is OpenCodeError.Unauthorized -> OpenCodeError.PairingFailed(error.message.orEmpty())
    is OpenCodeError.NotFound -> OpenCodeError.PairingFailed(error.message.orEmpty())
    is OpenCodeError.Server -> if (error.status in 400..499) OpenCodeError.PairingFailed(error.message.orEmpty()) else error
    else -> error
}

private fun parseErrorBody(body: String?): Pair<String?, String?> {
    if (body.isNullOrBlank()) return null to null
    val json = runCatching { OpenCodeJson.parseToJsonElement(body) as? JsonObject }.getOrNull()
        ?: return null to null
    fun field(name: String) = runCatching { json[name]?.jsonPrimitive?.contentOrNull }.getOrNull()
    return field("_tag") to field("message")
}
