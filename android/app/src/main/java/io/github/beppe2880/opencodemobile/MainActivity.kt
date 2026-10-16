package io.github.beppe2880.opencodemobile

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.beppe2880.opencodemobile.core.domain.ServerRepository
import io.github.beppe2880.opencodemobile.core.domain.SettingsRepository
import io.github.beppe2880.opencodemobile.core.ui.theme.OcTheme
import io.github.beppe2880.opencodemobile.core.ui.theme.isDark
import io.github.beppe2880.opencodemobile.navigation.AppNavHost
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val servers: ServerRepository by inject()
    private val settings: SettingsRepository by inject()

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        keepSplashUntilLoaded()
        setContent {
            val appSettings by settings.settings.collectAsStateWithLifecycle()
            val loaded by servers.loaded.collectAsStateWithLifecycle()
            val activeServer by servers.activeServer.collectAsStateWithLifecycle()
            val dark = appSettings.theme.isDark()
            DisposableEffect(dark) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            OcTheme(darkTheme = dark) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(OcTheme.colors.bgBase)
                        .semantics { testTagsAsResourceId = true },
                ) {
                    // Choosing the start screen needs the saved servers, read off the main thread.
                    if (loaded) AppNavHost(hasActiveServer = activeServer != null)
                }
            }
        }
    }

    /** Holds the first frame (the system splash screen) until the saved servers are read. */
    private fun keepSplashUntilLoaded() {
        if (servers.loaded.value) return
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (!servers.loaded.value) return false
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            },
        )
    }
}
