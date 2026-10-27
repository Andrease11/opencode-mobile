package io.github.beppe2880.opencodemobile.core.data.events

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_get_global_queue

/**
 * [NetworkMonitor] backed by `NWPathMonitor`: reports each time the path becomes usable again
 * after being unusable. The first update (the current path) is not reported.
 */
internal class PathNetworkMonitor : NetworkMonitor {
    override val networkAvailable: Flow<Unit> = callbackFlow {
        val monitor = nw_path_monitor_create()
        var first = true
        var satisfied = false
        nw_path_monitor_set_update_handler(monitor) { path ->
            val now = nw_path_get_status(path) == nw_path_status_satisfied
            if (!first && now && !satisfied) trySend(Unit)
            first = false
            satisfied = now
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u))
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }
}
