package io.github.beppe2880.opencodemobile.feature.chat

import android.content.ClipData
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.beppe2880.opencodemobile.core.mvi.CollectEffect
import io.github.beppe2880.opencodemobile.core.ui.R as UiR
import io.github.beppe2880.opencodemobile.core.ui.text.asString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChatRoute(
    sessionId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    onOpenSession: (String) -> Unit = {},
    /** Replaces this chat with another session of the list (swipe). */
    onSwitchSession: (String) -> Unit = {},
) {
    val viewModel: ChatViewModel = koinViewModel(key = "chat-$sessionId") { parametersOf(sessionId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    // Per session: in list + detail the same route shows another session after a swipe.
    val listState = rememberSaveable(sessionId, saver = LazyListState.Saver) { LazyListState() }

    val readAttachments: (List<Uri>) -> Unit = { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val read = withContext(Dispatchers.IO) { uris.map { AttachmentReader.read(context.contentResolver, it) } }
                if (read.any { it == null }) viewModel.onEvent(ChatEvent.AttachmentFailed)
                read.filterNotNull().takeIf { it.isNotEmpty() }?.let { viewModel.onEvent(ChatEvent.AddAttachments(it)) }
            }
        }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(MAX_PICKED), readAttachments)
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments(), readAttachments)

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is ChatEffect.ShowMessage -> snackbar.showSnackbar(effect.message.asString(context))
            ChatEffect.NavigateBack -> onBack()
            ChatEffect.PromptSent -> listState.scrollToItem(0)
            is ChatEffect.OpenSession -> onOpenSession(effect.sessionId)
            is ChatEffect.SwitchSession -> onSwitchSession(effect.sessionId)
        }
    }

    ChatScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onCopy = { text ->
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("OpenCode", text)))
                snackbar.showSnackbar(context.getString(UiR.string.oc_copied))
            }
        },
        modifier = modifier,
        onAttachPhoto = {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onAttachFile = { filePicker.launch(arrayOf("*/*")) },
        showBack = showBack,
        snackbarHostState = snackbar,
        listState = listState,
    )
}

private const val MAX_PICKED = 5
