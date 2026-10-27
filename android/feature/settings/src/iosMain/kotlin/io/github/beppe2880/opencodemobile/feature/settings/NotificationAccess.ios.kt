package io.github.beppe2880.opencodemobile.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// Session notifications need a background monitor that only exists on Android for now.
actual val notificationsSupported: Boolean = false

@Composable
internal actual fun rememberNotificationAccess(onPermissionResult: (Boolean) -> Unit): NotificationAccess = remember {
    object : NotificationAccess {
        override fun allowed() = true

        override fun request() = Unit

        override fun openSettings() = Unit
    }
}
