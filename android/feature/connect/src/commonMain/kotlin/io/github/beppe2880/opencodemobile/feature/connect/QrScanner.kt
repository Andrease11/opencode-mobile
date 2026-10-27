package io.github.beppe2880.opencodemobile.feature.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.beppe2880.opencodemobile.core.ui.components.OcButton
import io.github.beppe2880.opencodemobile.core.ui.components.OcButtonVariant
import io.github.beppe2880.opencodemobile.core.ui.components.OcEmptyState
import io.github.beppe2880.opencodemobile.core.ui.components.OcIconButton
import io.github.beppe2880.opencodemobile.core.ui.platform.OcBackHandler
import io.github.beppe2880.opencodemobile.core.ui.resources.oc_close
import io.github.beppe2880.opencodemobile.core.ui.theme.OcDarkColors
import io.github.beppe2880.opencodemobile.core.ui.theme.OcRadius
import io.github.beppe2880.opencodemobile.core.ui.theme.OcSpacing
import io.github.beppe2880.opencodemobile.core.ui.theme.OcTheme
import io.github.beppe2880.opencodemobile.feature.connect.resources.Res
import io.github.beppe2880.opencodemobile.feature.connect.resources.connect_camera_denied
import io.github.beppe2880.opencodemobile.feature.connect.resources.connect_camera_grant
import io.github.beppe2880.opencodemobile.feature.connect.resources.connect_camera_rationale
import io.github.beppe2880.opencodemobile.feature.connect.resources.connect_camera_rationale_title
import io.github.beppe2880.opencodemobile.feature.connect.resources.connect_camera_unavailable
import io.github.beppe2880.opencodemobile.feature.connect.resources.connect_open_settings
import io.github.beppe2880.opencodemobile.feature.connect.resources.connect_scanner_hint
import io.github.beppe2880.opencodemobile.feature.connect.resources.connect_scanner_title
import org.jetbrains.compose.resources.stringResource
import io.github.beppe2880.opencodemobile.core.ui.resources.Res as UiRes

/**
 * Full-screen QR scanner overlay. Handles the camera permission with a rationale and a
 * settings fallback; the platform camera preview decodes frames and reports the first hit.
 */
@Composable
internal fun QrScannerOverlay(
    onResult: (String) -> Unit,
    onClose: () -> Unit,
) {
    val camera = rememberCameraAccess()
    LaunchedEffect(Unit) {
        if (!camera.granted && camera.hasCamera) camera.request()
    }
    OcBackHandler(onBack = onClose)

    val c = OcTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bgDeep)
            .testTag("connect_scanner"),
    ) {
        when {
            !camera.hasCamera -> PermissionMessage(
                message = stringResource(Res.string.connect_camera_unavailable),
                action = null,
            )
            camera.granted -> CameraPreview(onResult = onResult)
            camera.denied -> PermissionMessage(
                message = stringResource(Res.string.connect_camera_denied),
                action = {
                    OcButton(
                        text = stringResource(Res.string.connect_open_settings),
                        onClick = camera::openSettings,
                        variant = OcButtonVariant.Secondary,
                        modifier = Modifier.testTag("connect_camera_settings_button"),
                    )
                },
            )
            else -> PermissionMessage(
                message = stringResource(Res.string.connect_camera_rationale),
                title = stringResource(Res.string.connect_camera_rationale_title),
                action = {
                    OcButton(
                        text = stringResource(Res.string.connect_camera_grant),
                        onClick = camera::request,
                        modifier = Modifier.testTag("connect_camera_grant_button"),
                    )
                },
            )
        }
        ScannerChrome(granted = camera.granted && camera.hasCamera, onClose = onClose)
    }
}

/** Camera availability and permission, refreshed when the app returns from the system settings. */
@Stable
internal interface CameraAccess {
    val hasCamera: Boolean
    val granted: Boolean

    /** The user refused the permission; only the system settings can grant it now. */
    val denied: Boolean

    fun request()

    fun openSettings()
}

@Composable
internal expect fun rememberCameraAccess(): CameraAccess

/** Full-size live camera feed that reports the first decoded QR code once. */
@Composable
internal expect fun CameraPreview(onResult: (String) -> Unit)

@Composable
private fun ScannerChrome(granted: Boolean, onClose: () -> Unit) {
    // Over the camera feed the chrome always uses the dark palette, whatever the app theme.
    val overlay = OcDarkColors
    val onDark = if (granted) overlay.textBase else OcTheme.colors.textBase
    Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(OcSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OcIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(UiRes.string.oc_close),
                onClick = onClose,
                tint = onDark,
                modifier = Modifier.testTag("connect_scanner_close_button"),
            )
            Text(stringResource(Res.string.connect_scanner_title), style = MaterialTheme.typography.titleSmall, color = onDark)
        }
        if (granted) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .border(2.dp, overlay.textBase, RoundedCornerShape(OcRadius.xl)),
                )
            }
            Text(
                text = stringResource(Res.string.connect_scanner_hint),
                color = overlay.textBase,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(OcSpacing.xl)
                    .background(overlay.scrim, RoundedCornerShape(OcRadius.md))
                    .padding(OcSpacing.md),
            )
        }
    }
}

@Composable
private fun PermissionMessage(message: String, action: (@Composable () -> Unit)?, title: String? = null) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        OcEmptyState(
            icon = Icons.Outlined.PhotoCamera,
            title = title ?: stringResource(Res.string.connect_scanner_title),
            message = message,
            action = action,
        )
    }
}
