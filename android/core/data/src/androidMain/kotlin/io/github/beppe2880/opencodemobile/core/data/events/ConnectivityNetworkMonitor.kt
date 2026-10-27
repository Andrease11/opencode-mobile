package io.github.beppe2880.opencodemobile.core.data.events

import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.atomic.AtomicReference

/** [NetworkMonitor] backed by a default network callback, registered only while collected. */
internal class ConnectivityNetworkMonitor(private val connectivity: ConnectivityManager) : NetworkMonitor {
    override val networkAvailable: Flow<Unit> = callbackFlow {
        // The callback fires onAvailable right after registration for the current network.
        val current = AtomicReference<Network?>(connectivity.activeNetwork)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (current.getAndSet(network) != network) trySend(Unit)
            }

            override fun onLost(network: Network) {
                current.compareAndSet(network, null)
            }
        }
        connectivity.registerDefaultNetworkCallback(callback)
        awaitClose { connectivity.unregisterNetworkCallback(callback) }
    }
}
