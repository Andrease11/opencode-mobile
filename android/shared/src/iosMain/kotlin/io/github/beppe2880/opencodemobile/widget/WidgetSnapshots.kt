package io.github.beppe2880.opencodemobile.widget

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.beppe2880.opencodemobile.core.domain.EventRepository
import io.github.beppe2880.opencodemobile.core.domain.ServerRepository
import io.github.beppe2880.opencodemobile.core.domain.SettingsRepository
import io.github.beppe2880.opencodemobile.core.ui.theme.OcColors
import io.github.beppe2880.opencodemobile.core.ui.theme.OcDarkColors
import io.github.beppe2880.opencodemobile.core.ui.theme.OcLightColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import kotlin.time.Clock

/**
 * Feeds the WidgetKit extension (iosApp/SessionsWidget), which cannot reach the server: the
 * app writes what the widget shows to the App Group defaults and asks WidgetKit to reload.
 * Without an App Group (`OCAppGroup` in Info.plist) nothing is written.
 */
internal class WidgetSnapshots(
    private val loader: WidgetLoader,
    private val events: EventRepository,
    private val servers: ServerRepository,
    private val settings: SettingsRepository,
    private val reloadWidgets: () -> Unit,
) {
    private val defaults: NSUserDefaults? =
        (NSBundle.mainBundle.objectForInfoDictionaryKey("OCAppGroup") as? String)?.let { NSUserDefaults(suiteName = it) }

    fun start(scope: CoroutineScope) {
        val defaults = defaults ?: return
        scope.launch {
            write(defaults)
            widgetRefreshes(events, servers, appVisible()).collect { write(defaults) }
        }
    }

    private suspend fun write(defaults: NSUserDefaults) {
        val snapshot = when (val model = loader.load()) {
            WidgetModel.NoServer -> WidgetSnapshot(connected = false)
            is WidgetModel.Sessions -> WidgetSnapshot(
                connected = true,
                serverName = model.serverName,
                stale = model.stale,
                sessions = model.sessions.map { WidgetSnapshot.Row(it.id, it.title, it.project, it.updatedAt, it.running) },
            )
        }.copy(
            theme = settings.settings.value.theme.name,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
            light = OcLightColors.palette(),
            dark = OcDarkColors.palette(),
        )
        defaults.setObject(SnapshotJson.encodeToString(WidgetSnapshot.serializer(), snapshot), forKey = KEY)
        reloadWidgets()
    }

    private fun appVisible(): Flow<Boolean> = callbackFlow {
        val center = NSNotificationCenter.defaultCenter
        val queue = NSOperationQueue.mainQueue
        val background = center.addObserverForName(UIApplicationDidEnterBackgroundNotification, null, queue) { trySend(false) }
        val foreground = center.addObserverForName(UIApplicationWillEnterForegroundNotification, null, queue) { trySend(true) }
        trySend(true)
        awaitClose {
            center.removeObserver(background)
            center.removeObserver(foreground)
        }
    }

    private companion object {
        /** Read by iosApp/SessionsWidget/SessionsWidget.swift. */
        const val KEY = "widget.snapshot"
        val SnapshotJson = Json { encodeDefaults = true }
    }
}

/** The JSON contract with SessionsWidget.swift; colors are `#RRGGBB`. */
@Serializable
internal data class WidgetSnapshot(
    val connected: Boolean,
    val serverName: String? = null,
    val stale: Boolean = false,
    val sessions: List<Row> = emptyList(),
    /** [io.github.beppe2880.opencodemobile.core.domain.ThemeMode] name. */
    val theme: String = "System",
    val updatedAt: Long = 0,
    val light: Palette = Palette(),
    val dark: Palette = Palette(),
) {
    @Serializable
    data class Row(val id: String, val title: String, val project: String, val updatedAt: Long, val running: Boolean)

    @Serializable
    data class Palette(
        val background: String = "",
        val surface: String = "",
        val text: String = "",
        val muted: String = "",
        val accent: String = "",
        val onAccent: String = "",
        val running: String = "",
    )
}

private fun OcColors.palette() = WidgetSnapshot.Palette(
    background = bgBase.hex(),
    surface = layer02.hex(),
    text = textBase.hex(),
    muted = textMuted.hex(),
    accent = bgAccent.hex(),
    onAccent = onAccent.hex(),
    running = success.fg.hex(),
)

private fun Color.hex(): String = "#" + (toArgb() and 0xFFFFFF).toString(16).uppercase().padStart(6, '0')
