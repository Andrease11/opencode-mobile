package io.github.beppe2880.opencodemobile.core.data.events

import kotlinx.coroutines.flow.Flow

/** Whether any app UI is visible; live connections pause while it is false. */
internal interface AppVisibility {
    val isVisible: Flow<Boolean>
}
