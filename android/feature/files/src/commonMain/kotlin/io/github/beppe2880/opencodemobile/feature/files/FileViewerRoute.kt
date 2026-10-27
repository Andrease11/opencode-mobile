package io.github.beppe2880.opencodemobile.feature.files

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.beppe2880.opencodemobile.core.mvi.CollectEffect
import io.github.beppe2880.opencodemobile.core.ui.platform.rememberTextClipboard
import io.github.beppe2880.opencodemobile.core.ui.resources.oc_copied
import io.github.beppe2880.opencodemobile.core.ui.text.asString
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import io.github.beppe2880.opencodemobile.core.ui.resources.Res as UiRes

/** Read-only view of [path] (absolute, or relative to the session folder [directory]). */
@Composable
fun FileViewerRoute(directory: String, path: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: FileViewerViewModel = koinViewModel(key = "file-$directory-$path") {
        parametersOf(FileTarget(directory, path))
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val clipboard = rememberTextClipboard()
    val scope = rememberCoroutineScope()
    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            FileViewerEffect.NavigateBack -> onBack()
            is FileViewerEffect.ShowMessage -> snackbar.showSnackbar(effect.message.asString())
        }
    }
    FileViewerScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onCopy = { text ->
            scope.launch {
                clipboard.setText(text)
                snackbar.showSnackbar(getString(UiRes.string.oc_copied))
            }
        },
        modifier = modifier,
        snackbarHostState = snackbar,
    )
}
