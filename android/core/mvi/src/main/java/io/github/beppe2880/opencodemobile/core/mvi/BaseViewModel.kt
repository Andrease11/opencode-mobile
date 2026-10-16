package io.github.beppe2880.opencodemobile.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * Base class for MVI ViewModels.
 *
 * Removes the boilerplate every ViewModel used to repeat by hand:
 * - a [MutableStateFlow] exposed as a read-only [StateFlow] ([state]);
 * - a buffered [Channel] of one-shot effects exposed as a [Flow] ([effect]);
 * - [updateState] / [currentState] / [sendEffect] helpers.
 *
 * Screens keep consuming `viewModel.state` and `viewModel.effect` exactly as before
 * (collect `state` with `collectAsStateWithLifecycle`, collect `effect` with [CollectEffect]).
 *
 * Subclasses only implement [onEvent] and call the helpers:
 *
 * ```
 * class TasksViewModel(...) : BaseViewModel<TasksState, TasksEvent, TasksEffect>(TasksState()) {
 *     override fun onEvent(event: TasksEvent) { ... }
 *     private fun load() = updateState { it.copy(isLoading = true) }
 * }
 * ```
 *
 * @param initialState the state emitted before any event is handled.
 */
abstract class BaseViewModel<State, Event, Effect>(
    initialState: State,
) : ViewModel() {

    private val _state = OptimisticState(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    /** Current state snapshot, equivalent to the old `_state.value`. */
    protected val currentState: State get() = _state.value

    /** Single entry point for UI events. */
    abstract fun onEvent(event: Event)

    /** Atomically update the state, equivalent to the old `_state.update { ... }`. */
    protected fun updateState(transform: (State) -> State) = _state.update(transform)

    protected fun optimisticUpdate(transform: (State) -> State) = _state.begin(transform)

    private val pendingMutations = mutableSetOf<String>()

    protected fun isMutationPending(key: String): Boolean = key in pendingMutations

    /** A key serializes conflicting writes; independent changes can remain in flight. */
    @OptIn(ExperimentalCoroutinesApi::class)
    protected fun runOptimistic(
        key: String,
        transform: (State) -> State,
        onError: (String) -> Unit,
        onSettled: () -> Unit = {},
        request: suspend () -> ((State) -> State)?,
    ) {
        if (!pendingMutations.add(key)) return
        val change = optimisticUpdate(transform)
        // Enter try/finally even when the scope is cancelled before dispatch.
        viewModelScope.launch(start = CoroutineStart.ATOMIC) {
            try {
                coroutineContext.ensureActive()
                change.confirm(request())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                onError(error.message ?: "Unable to save changes")
            } finally {
                change.rollback()
                pendingMutations.remove(key)
                onSettled()
            }
        }
    }

    /** Emit a one-shot effect, equivalent to the old `_effect.trySend(...)`. */
    protected fun sendEffect(effect: Effect) {
        _effect.trySend(effect)
    }
}
