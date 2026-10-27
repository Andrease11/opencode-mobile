package io.github.beppe2880.opencodemobile.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.beppe2880.opencodemobile.core.mvi.CollectEffect
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onAddServer: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenUsage: () -> Unit = {},
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notifications = rememberNotificationAccess { granted ->
        viewModel.onEvent(SettingsEvent.NotificationAccess(allowed = granted, blocked = !granted))
    }
    // The user can change notification access in the system settings at any time.
    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(SettingsEvent.NotificationAccess(allowed = notifications.allowed()))
        onPauseOrDispose { }
    }
    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            SettingsEffect.NavigateBack -> onBack()
            SettingsEffect.OpenConnect -> onAddServer()
            SettingsEffect.OpenUsage -> onOpenUsage()
            SettingsEffect.RequestNotificationPermission -> notifications.request()
            SettingsEffect.OpenNotificationSettings -> notifications.openSettings()
        }
    }
    SettingsScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}
