package io.github.beppe2880.opencodemobile.core.data.server

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import io.github.beppe2880.opencodemobile.core.domain.ServerDiscovery
import io.github.beppe2880.opencodemobile.core.network.LanScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.net.Inet4Address

/**
 * Scans the default OpenCode port on the IPv4 subnets of the phone's Wi-Fi and Ethernet
 * networks. VPN tunnels are skipped: their peers are not on a subnet the phone can enumerate.
 */
internal class LanServerDiscovery(
    private val connectivity: ConnectivityManager,
    private val scanner: LanScanner = LanScanner(),
) : ServerDiscovery {

    override fun discover(): Flow<String> = flow { emitAll(scanner.scan(localHosts())) }

    @Suppress("DEPRECATION") // allNetworks: the callback API is overkill for a one-shot scan.
    private fun localHosts(): List<String> = connectivity.allNetworks
        .filter { network ->
            val caps = connectivity.getNetworkCapabilities(network) ?: return@filter false
            !caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) &&
                (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        }
        .flatMap { connectivity.getLinkProperties(it)?.linkAddresses.orEmpty() }
        .filter { it.address is Inet4Address && !it.address.isLoopbackAddress }
        .flatMap { LanScanner.subnetHosts(it.address.address, it.prefixLength) }
        .distinct()
}
