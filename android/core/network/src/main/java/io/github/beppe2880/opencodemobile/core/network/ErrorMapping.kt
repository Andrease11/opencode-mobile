package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.core.domain.OpenCodeError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException

private const val MAX_ERROR_BODY = 16L * 1024

/** Awaits a generated API call and returns its body, throwing [OpenCodeError] on any failure. */
suspend fun <T : Any> awaitBody(call: suspend () -> Response<T>): T {
    val response = awaitResponse(call)
    return response.body() ?: throw OpenCodeError.Server(response.code(), null, "Empty response body")
}

/** Awaits a generated API call whose body is irrelevant, throwing [OpenCodeError] on any failure. */
suspend fun <T> awaitSuccess(call: suspend () -> Response<T>) {
    awaitResponse(call)
}

private suspend fun <T> awaitResponse(call: suspend () -> Response<T>): Response<T> {
    val response = try {
        call()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw e.toOpenCodeError()
    }
    if (!response.isSuccessful) {
        throw httpError(response.code(), response.errorBody()?.readBounded())
    }
    return response
}

internal fun ResponseBody.readBounded(): String = use { body ->
    val source = body.source()
    source.request(MAX_ERROR_BODY)
    source.buffer.readUtf8(minOf(source.buffer.size, MAX_ERROR_BODY))
}

/** Maps transport and decoding failures; [OpenCodeError]s pass through unchanged. */
fun Throwable.toOpenCodeError(): OpenCodeError = when (this) {
    is OpenCodeError -> this
    is IOException -> OpenCodeError.Unreachable(message ?: javaClass.simpleName, this)
    is SerializationException ->
        OpenCodeError.Server(status = 0, tag = null, message = "Unexpected server response: $message")
    else -> OpenCodeError.Server(status = 0, tag = null, message = message ?: javaClass.simpleName)
}

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
