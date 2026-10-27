package io.github.beppe2880.opencodemobile.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Lifecycle-aware effect collector.
 *
 * Collects one-shot effects only when the composable's lifecycle is at least STARTED.
 * Prevents effects from being delivered when the app is in the background
 * (which could cause crashes with navigation or snackbar calls).
 */
@Composable
fun <T> CollectEffect(flow: Flow<T>, onEffect: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { onEffect(it) }
        }
    }
}
