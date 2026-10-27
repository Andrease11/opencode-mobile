package io.github.beppe2880.opencodemobile.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/** Session notifications exist on this platform; when false their settings are hidden. */
expect val notificationsSupported: Boolean

/** System notification permission for this app. */
@Stable
internal interface NotificationAccess {
    /** The system currently lets the app post notifications. */
    fun allowed(): Boolean

    /** Asks for the permission; the answer goes to the callback of [rememberNotificationAccess]. */
    fun request()

    fun openSettings()
}

@Composable
internal expect fun rememberNotificationAccess(onPermissionResult: (Boolean) -> Unit): NotificationAccess
