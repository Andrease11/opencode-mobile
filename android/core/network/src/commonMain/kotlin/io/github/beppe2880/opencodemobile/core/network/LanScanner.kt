package io.github.beppe2880.opencodemobile.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.readBuffer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Looks for OpenCode servers on a local subnet. Every host is asked for `/site.webmanifest`,
 * which OpenCode serves without authentication with the name "OpenCode"; an open port or a
 * 401 alone could be any other service.
 */
class LanScanner(
    private val http: HttpClient = defaultClient(),
    private val parallelism: Int = 64,
) {

    /** Emits the base URL of every OpenCode server among [hosts] as soon as it answers. */
    fun scan(hosts: List<String>, port: Int = ConnectLinkParser.DEFAULT_PORT): Flow<String> = channelFlow {
        val permits = Semaphore(parallelism)
        hosts.distinct().forEach { host ->
            launch {
                val baseUrl = "http://$host:$port"
                if (permits.withPermit { isOpenCode(baseUrl) }) send(baseUrl)
            }
        }
    }

    internal suspend fun isOpenCode(baseUrl: String): Boolean = try {
        http.prepareGet("$baseUrl/site.webmanifest").execute { response ->
            response.status.isSuccess() &&
                manifestName(response.bodyAsChannel().readBuffer(MAX_MANIFEST_BYTES).readString()) == "OpenCode"
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }

    companion object {
        private const val MAX_MANIFEST_BYTES = 16_384

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

        private fun defaultClient(): HttpClient = HttpClient {
            expectSuccess = false
            followRedirects = false
            install(HttpTimeout) {
                connectTimeoutMillis = 800
                socketTimeoutMillis = 1_500
                requestTimeoutMillis = 2_500
            }
        }
    }
}
