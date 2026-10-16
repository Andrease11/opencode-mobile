package io.github.beppe2880.opencodemobile.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.beppe2880.opencodemobile.core.mvi.CollectEffect
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onAddServer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            SettingsEffect.NavigateBack -> onBack()
            SettingsEffect.OpenConnect -> onAddServer()
        }
    }
    SettingsScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}
