package io.github.beppe2880.opencodemobile.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin

internal actual fun platformHttpClient(pingIntervalMillis: Long, block: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(Darwin) {
    block()
    // After HttpTimeout, which sets the connect timeout it reads.
    install(ConnectProbe)
}
