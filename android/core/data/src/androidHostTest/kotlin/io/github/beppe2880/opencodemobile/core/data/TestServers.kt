package io.github.beppe2880.opencodemobile.core.data

import io.github.beppe2880.opencodemobile.core.data.server.ServerStore
import io.github.beppe2880.opencodemobile.core.data.server.StoredServerRepository
import io.github.beppe2880.opencodemobile.core.data.server.StoredServers
import io.github.beppe2880.opencodemobile.core.domain.CredentialKind
import io.github.beppe2880.opencodemobile.core.domain.ServerProfile
import io.github.beppe2880.opencodemobile.core.network.OpenCodeClientFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.mockwebserver.MockWebServer

internal class FakeServerStore(var stored: StoredServers = StoredServers()) : ServerStore {
    var saves = 0
    override fun load() = stored
    override fun save(servers: StoredServers) {
        stored = servers
        saves++
    }
}

internal fun MockWebServer.baseUrl(): String = url("/").toString().trimEnd('/')

internal fun profile(id: String, baseUrl: String, addedAt: Long = 1, secret: String = "pw") =
    ServerProfile(id = id, name = id, baseUrl = baseUrl, secret = secret, credential = CredentialKind.Password, addedAt = addedAt)

internal class TestGraph(
    store: FakeServerStore,
    ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
) {
    val factory = OpenCodeClientFactory()
    val servers = StoredServerRepository(store, factory, ioDispatcher, clock = { 100 }, newId = { "new-id" })
    val clients = OpenCodeClients(factory, servers)

    companion object {
        /** A graph whose active server is [server]. */
        fun activeOn(server: MockWebServer) =
            TestGraph(FakeServerStore(StoredServers(listOf(profile("s1", server.baseUrl())), activeId = "s1")))
    }
}
