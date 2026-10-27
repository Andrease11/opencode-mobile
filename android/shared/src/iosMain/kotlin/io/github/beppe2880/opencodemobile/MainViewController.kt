package io.github.beppe2880.opencodemobile

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import io.github.beppe2880.opencodemobile.api.OpenCodeApiVersion
import io.github.beppe2880.opencodemobile.core.data.di.dataModule
import io.github.beppe2880.opencodemobile.feature.settings.AppInfo
import org.koin.core.context.startKoin
import platform.Foundation.NSBundle
import platform.UIKit.UIViewController

private var koinStarted = false

/** Entry point used by iosApp: starts Koin once and returns the Compose root controller. */
@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
    if (!koinStarted) {
        val version = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "dev"
        startKoin { modules(dataModule, sharedModule(AppInfo(versionName = version, apiVersion = OpenCodeApiVersion.OPENCODE))) }
        koinStarted = true
    }
    // The screens move content above the keyboard themselves (imePadding); the default
    // behavior would shift the focused field a second time.
    return ComposeUIViewController(configure = { onFocusBehavior = OnFocusBehavior.DoNothing }) { OpenCodeApp() }
}
