package io.github.beppe2880.opencodemobile.core.network

import android.util.Log
import io.github.beppe2880.opencodemobile.api.models.SessionInfo
import io.github.beppe2880.opencodemobile.api.models.SessionMessageAssistantContentInner
import io.github.beppe2880.opencodemobile.api.models.SessionMessageInfo
import io.github.beppe2880.opencodemobile.api.models.SessionMessagesResponse
import io.github.beppe2880.opencodemobile.api.models.SessionMessagesResponseCursor
import io.github.beppe2880.opencodemobile.api.models.SessionsResponse
import io.github.beppe2880.opencodemobile.api.models.SessionsResponseCursor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Items of a list page decoded one by one. [received] counts what the server sent, skipped
 * items included, so paging can still compare it with the requested page size.
 */
class TolerantList<T> internal constructor(private val items: List<T>, val received: Int) : List<T> by items {
    override fun equals(other: Any?): Boolean = items == other
    override fun hashCode(): Int = items.hashCode()
    override fun toString(): String = items.toString()
}

/** How many items the server sent for this page, including those that could not be decoded. */
val List<*>.receivedCount: Int get() = (this as? TolerantList<*>)?.received ?: size

/**
 * Decodes the message and session list pages item by item with the generated serializers.
 * An item the client cannot decode (e.g. a message type added by a newer server) is skipped
 * instead of failing the whole page. An assistant part of an unknown type is dropped from its
 * message, which is kept. Skips are logged as counts and types only, never content.
 */
internal object TolerantPages {

    private const val TAG = "OpenCodeDecoding"

    /** Called with the list kind, how many items were skipped and their `type` values. Tests replace it. */
    @Volatile
    internal var onSkipped: (kind: String, count: Int, types: List<String>) -> Unit = { kind, count, types ->
        // android.util.Log is a stub in JVM unit tests.
        runCatching { Log.w(TAG, "Skipped $count undecodable $kind (types: ${types.joinToString()})") }
    }

    fun messages(json: Json, body: String): SessionMessagesResponse {
        val root = json.parseToJsonElement(body).jsonObject
        return SessionMessagesResponse(
            data = items(json, root["data"], "messages", ::decodeMessage),
            cursor = root["cursor"]?.let { json.decodeFromJsonElement(SessionMessagesResponseCursor.serializer(), it) }
                ?: SessionMessagesResponseCursor(),
        )
    }

    fun sessions(json: Json, body: String): SessionsResponse {
        val root = json.parseToJsonElement(body).jsonObject
        return SessionsResponse(
            data = items(json, root["data"], "sessions") { j, e -> j.decodeFromJsonElement(SessionInfo.serializer(), e) },
            cursor = root["cursor"]?.let { json.decodeFromJsonElement(SessionsResponseCursor.serializer(), it) }
                ?: SessionsResponseCursor(),
        )
    }

    /** Decodes one message; on failure retries without assistant parts that do not decode on their own. */
    internal fun decodeMessage(json: Json, element: JsonElement): SessionMessageInfo = try {
        json.decodeFromJsonElement(SessionMessageInfo.serializer(), element)
    } catch (e: SerializationException) {
        val repaired = withoutUnknownParts(json, element) ?: throw e
        json.decodeFromJsonElement(SessionMessageInfo.serializer(), repaired)
    }

    private fun withoutUnknownParts(json: Json, element: JsonElement): JsonElement? {
        val message = element as? JsonObject ?: return null
        val content = message["content"] as? JsonArray ?: return null
        val known = content.filter { part -> decodes(json, SessionMessageAssistantContentInner.serializer(), part) }
        if (known.size == content.size) return null
        return JsonObject(message + ("content" to JsonArray(known)))
    }

    private fun <T> decodes(json: Json, serializer: KSerializer<T>, element: JsonElement): Boolean = try {
        json.decodeFromJsonElement(serializer, element)
        true
    } catch (_: IllegalArgumentException) {
        // Also covers SerializationException; generated union serializers use require().
        false
    }

    private fun <T> items(json: Json, data: JsonElement?, kind: String, decode: (Json, JsonElement) -> T): List<T> {
        val array = data as? JsonArray ?: throw SerializationException("Missing $kind list")
        val skippedTypes = mutableListOf<String>()
        val decoded = array.mapNotNull { element ->
            try {
                decode(json, element)
            } catch (_: IllegalArgumentException) {
                skippedTypes += typeOf(element)
                null
            }
        }
        if (skippedTypes.isNotEmpty()) onSkipped(kind, skippedTypes.size, skippedTypes)
        return TolerantList(decoded, array.size)
    }

    /** The item's `type` discriminator, bounded; never any other field. */
    private fun typeOf(element: JsonElement): String =
        ((element as? JsonObject)?.get("type") as? JsonPrimitive)?.takeIf { it.isString }?.content?.take(40) ?: "?"
}

/** Routes the list pages to [TolerantPages]; everything else goes to the next converter. */
internal class TolerantPagesConverterFactory(private val json: Json) : Converter.Factory() {
    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? =
        when (type) {
            SessionMessagesResponse::class.java -> Converter<ResponseBody, SessionMessagesResponse> { body ->
                body.use { TolerantPages.messages(json, it.string()) }
            }
            SessionsResponse::class.java -> Converter<ResponseBody, SessionsResponse> { body ->
                body.use { TolerantPages.sessions(json, it.string()) }
            }
            else -> null
        }
}
