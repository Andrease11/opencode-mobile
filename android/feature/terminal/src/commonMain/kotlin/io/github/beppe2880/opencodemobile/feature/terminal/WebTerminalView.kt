package io.github.beppe2880.opencodemobile.feature.terminal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.beppe2880.opencodemobile.core.ui.theme.OcColors
import io.github.beppe2880.opencodemobile.core.ui.theme.OcTheme
import io.github.beppe2880.opencodemobile.core.ui.theme.terminalAnsi
import io.github.beppe2880.opencodemobile.feature.terminal.resources.Res
import kotlinx.coroutines.CancellationException
import kotlin.io.encoding.Base64
import io.github.beppe2880.opencodemobile.core.ui.resources.Res as UiRes

/**
 * The bundled page and what it needs: [uri] of `index.html` among its scripts, and IBM Plex
 * Mono, the app's monospace font, as base64 for the page to load.
 */
internal class TerminalPage(val uri: String, val fontBase64: String)

/** xterm.js in a web view that only loads the bundled page. */
@Composable
internal fun WebTerminalView(surface: WebSurface, openKeyboard: Boolean, modifier: Modifier = Modifier) {
    val colors = OcTheme.colors
    LaunchedEffect(surface, colors) { surface.setTheme(colors.xtermTheme()) }
    val font by produceState(TerminalFont.cached) { value = TerminalFont.load() }
    font?.let { PlatformTerminalView(surface, TerminalPage(Res.getUri(PAGE), it), openKeyboard, modifier) }
}

/** The platform web view showing [page]; [openKeyboard] is the setting to show the keyboard on entry. */
@Composable
internal expect fun PlatformTerminalView(surface: WebSurface, page: TerminalPage, openKeyboard: Boolean, modifier: Modifier)

private const val PAGE = "files/terminal/index.html"

private object TerminalFont {
    var cached: String? = null
        private set

    suspend fun load(): String = cached ?: try {
        Base64.encode(UiRes.readBytes("font/plex_mono_regular.ttf"))
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        // The page falls back to the system monospace font.
        ""
    }.also { cached = it }
}

internal fun OcColors.xtermTheme(): String {
    fun css(color: Color, alpha: String = "") =
        js("#" + (color.toArgb() and 0xFFFFFF).toString(16).uppercase().padStart(6, '0') + alpha)
    val names = listOf(
        "black", "red", "green", "yellow", "blue", "magenta", "cyan", "white",
        "brightBlack", "brightRed", "brightGreen", "brightYellow", "brightBlue", "brightMagenta", "brightCyan", "brightWhite",
    )
    val ansi = names.zip(terminalAnsi).joinToString(",") { (name, color) -> "$name:${css(color)}" }
    return "{background:${css(bgDeep)},foreground:${css(textBase)},cursor:${css(textBase)}," +
        "selectionBackground:${css(bgAccent, alpha = "66")},$ansi}"
}
