package io.github.beppe2880.opencodemobile.core.domain

import kotlinx.coroutines.flow.Flow

/** Finds OpenCode servers on the local network. Found servers still need a password or pairing code. */
interface ServerDiscovery {
    /** Emits the base URL of each server as soon as it answers, and completes when the scan ends. */
    fun discover(): Flow<String>
}
