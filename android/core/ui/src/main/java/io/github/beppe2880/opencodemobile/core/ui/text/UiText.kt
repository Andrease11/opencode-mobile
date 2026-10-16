package io.github.beppe2880.opencodemobile.core.ui.text

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import io.github.beppe2880.opencodemobile.core.domain.OpenCodeError
import io.github.beppe2880.opencodemobile.core.ui.R

/** Localizable text that ViewModels can put in State without a Context. */
@Immutable
sealed interface UiText {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
    data class Raw(val value: String) : UiText

    @Composable
    fun resolve(): String = when (this) {
        is Res -> stringResource(id, *args.toTypedArray())
        is Raw -> value
    }
}

/** Maps repository failures to user-facing messages, per [OpenCodeError] type. */
fun Throwable.toUiText(): UiText = when (this) {
    is OpenCodeError.Unauthorized -> UiText.Res(R.string.oc_error_unauthorized)
    is OpenCodeError.Unreachable -> UiText.Res(R.string.oc_error_unreachable)
    is OpenCodeError.PairingFailed -> UiText.Res(R.string.oc_error_pairing)
    is OpenCodeError.NotFound -> UiText.Res(R.string.oc_error_not_found)
    is OpenCodeError.Server -> UiText.Res(R.string.oc_error_server, listOf(status, message ?: tag ?: ""))
    else -> UiText.Res(R.string.oc_error_generic, listOf(message ?: javaClass.simpleName))
}

/** Resolves outside composition, e.g. for snackbars shown from effects. */
fun UiText.asString(context: android.content.Context): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res -> context.getString(id, *args.toTypedArray())
}
