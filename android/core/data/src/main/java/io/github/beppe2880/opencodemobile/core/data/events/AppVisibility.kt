package io.github.beppe2880.opencodemobile.core.data.events

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Whether any app UI is visible; live connections pause while it is false. */
internal interface AppVisibility {
    val isVisible: Flow<Boolean>
}

internal class ProcessLifecycleVisibility : AppVisibility {
    override val isVisible: Flow<Boolean> = ProcessLifecycleOwner.get().lifecycle.currentStateFlow
        .map { it.isAtLeast(Lifecycle.State.STARTED) }
        .distinctUntilChanged()
}
