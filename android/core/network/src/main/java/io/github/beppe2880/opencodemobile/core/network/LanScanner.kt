package io.github.beppe2880.opencodemobile.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Looks for OpenCode servers on a local subnet. Every host is asked for `/site.webmanifest`,
 * which OpenCode serves without authentication with the name "OpenCode"; an open port or a
 * 401 alone could be any other service.
 */
class LanScanner(
    private val http: OkHttpClient = defaultClient(),
    private val parallelism: Int = 64,
) {

    /** Emits the base URL of every OpenCode server among [hosts] as soon as it answers. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun scan(hosts: List<String>, port: Int = ConnectLinkParser.DEFAULT_PORT): Flow<String> = channelFlow {
        val io = Dispatchers.IO.limitedParallelism(parallelism)
        hosts.distinct().forEach { host ->
            launch(io) {
                val baseUrl = "http://$host:$port"
                if (isOpenCode(baseUrl)) send(baseUrl)
            }
        }
    }

    internal fun isOpenCode(baseUrl: String): Boolean = runCatching {
        val request = Request.Builder().url("$baseUrl/site.webmanifest").build()
        http.newCall(request).execute().use { response ->
            response.isSuccessful && manifestName(response.peekBody(MAX_MANIFEST_BYTES).string()) == "OpenCode"
        }
    }.getOrDefault(false)

    companion object {
        private const val MAX_MANIFEST_BYTES = 16_384L

        /**
         * Host addresses of the IPv4 subnet of [address] (4 bytes). Subnets larger than /24 are
         * narrowed to the /24 around [address] to keep the scan to at most 254 hosts.
         */
        fun subnetHosts(address: ByteArray, prefixLength: Int): List<String> {
            if (address.size != 4 || prefixLength !in 0..30) return emptyList()
            val prefix = maxOf(prefixLength, 24)
            val ip = address.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }
            val mask = (0xFFFFFFFFL shl (32 - prefix)) and 0xFFFFFFFFL
            val network = ip and mask
            val broadcast = network or (mask.inv() and 0xFFFFFFFFL)
            return ((network + 1) until broadcast).map { host ->
                "${host shr 24 and 0xFF}.${host shr 16 and 0xFF}.${host shr 8 and 0xFF}.${host and 0xFF}"
            }
        }

        private fun manifestName(body: String): String? = runCatching {
            Json.parseToJsonElement(body).jsonObject["name"]?.jsonPrimitive?.content
        }.getOrNull()

        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(800, TimeUnit.MILLISECONDS)
            .readTimeout(1500, TimeUnit.MILLISECONDS)
            .callTimeout(2500, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .build()
    }
}
