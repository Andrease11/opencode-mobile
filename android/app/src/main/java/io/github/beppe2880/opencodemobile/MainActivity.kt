package io.github.beppe2880.opencodemobile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.beppe2880.opencodemobile.core.domain.ServerRepository
import io.github.beppe2880.opencodemobile.core.domain.SettingsRepository
import io.github.beppe2880.opencodemobile.notifications.NotificationsIntro
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val servers: ServerRepository by inject()
    private val settings: SettingsRepository by inject()

    /** Session to open, from a tapped notification; cleared once navigated. */
    private val sessionToOpen = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) readSessionToOpen(intent)
        keepSplashUntilLoaded()
        setContent {
            val openSession by sessionToOpen.collectAsStateWithLifecycle()
            OpenCodeApp(
                modifier = Modifier.semantics { testTagsAsResourceId = true },
                openSessionId = openSession,
                onSessionOpened = { sessionToOpen.value = null },
                onDarkThemeChange = { dark ->
                    val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                },
                overlay = { appSettings -> if (!appSettings.notificationsIntroShown) NotificationsIntro(settings) },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readSessionToOpen(intent)
    }

    /** The launcher activity is exported: accept only a well-formed session id. */
    private fun readSessionToOpen(intent: Intent?) {
        val id = intent?.getStringExtra(EXTRA_SESSION_ID) ?: return
        intent.removeExtra(EXTRA_SESSION_ID)
        if (SESSION_ID.matches(id)) sessionToOpen.value = id
    }

    companion object {
        const val EXTRA_SESSION_ID = "io.github.beppe2880.opencodemobile.extra.SESSION_ID"
        private val SESSION_ID = Regex("ses_[A-Za-z0-9]{1,64}")
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
