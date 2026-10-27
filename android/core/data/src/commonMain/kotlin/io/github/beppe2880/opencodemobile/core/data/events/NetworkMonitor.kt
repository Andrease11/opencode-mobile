package io.github.beppe2880.opencodemobile.core.data.events

import kotlinx.coroutines.flow.Flow

/** Signals that let a waiting reconnect retry immediately. */
internal interface NetworkMonitor {
    /**
     * Emits each time a new default network becomes available while collected; the network
     * already present when collection starts is not reported.
     */
    val networkAvailable: Flow<Unit>
}
