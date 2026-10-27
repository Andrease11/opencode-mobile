package io.github.beppe2880.opencodemobile.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

// OkHttp ignores the WebSockets plugin's ping interval and sends pings itself.
internal actual fun platformHttpClient(pingIntervalMillis: Long, block: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(OkHttp) {
    engine { config { pingInterval(pingIntervalMillis, TimeUnit.MILLISECONDS) } }
    block()
}
