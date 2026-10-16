package io.github.beppe2880.opencodemobile.feature.connect

import androidx.compose.runtime.Immutable
import io.github.beppe2880.opencodemobile.core.domain.ServerProfile
import io.github.beppe2880.opencodemobile.core.ui.text.UiText

enum class AuthMethod { PairingCode, Password }

@Immutable
sealed interface ProbeResult {
    data class Reachable(val version: String) : ProbeResult

    /** Server answered but needs the pairing code to authenticate. */
    data object NeedsPairing : ProbeResult
    data class Failed(val message: UiText) : ProbeResult
}

@Immutable
data class ConnectState(
    val url: String = "",
    val method: AuthMethod = AuthMethod.PairingCode,
    val secret: String = "",
    val urlError: UiText? = null,
    val secretError: UiText? = null,
    val probing: Boolean = false,
    val probe: ProbeResult? = null,
    val submitting: Boolean = false,
    val submitError: UiText? = null,
    val servers: List<ServerProfile> = emptyList(),
    val activeServerId: String? = null,
    val scannerOpen: Boolean = false,
    /** Base URLs of OpenCode servers found on the local network, in the order they answered. */
    val discovered: List<String> = emptyList(),
    val discovering: Boolean = false,
) {
    val busy: Boolean get() = probing || submitting
    val canSubmit: Boolean get() = url.isNotBlank() && secret.isNotBlank() && !busy

    /** Found servers that are not saved yet. */
    val newDiscovered: List<String> get() = discovered.filterNot { url -> servers.any { it.baseUrl == url } }
}

sealed interface ConnectEvent {
    data class UrlChanged(val value: String) : ConnectEvent
    data class SecretChanged(val value: String) : ConnectEvent
    data class MethodChanged(val method: AuthMethod) : ConnectEvent
    data object TestConnection : ConnectEvent
    data object Submit : ConnectEvent
    data class LinkPasted(val text: String?) : ConnectEvent
    data object OpenScanner : ConnectEvent
    data object CloseScanner : ConnectEvent
    data class QrScanned(val text: String) : ConnectEvent
    data class SelectServer(val serverId: String) : ConnectEvent
    data class UseDiscovered(val baseUrl: String) : ConnectEvent
    data object Rediscover : ConnectEvent
}

sealed interface ConnectEffect {
    data object Connected : ConnectEffect
    data class ShowMessage(val message: UiText) : ConnectEffect
    data object FocusSecret : ConnectEffect
}
