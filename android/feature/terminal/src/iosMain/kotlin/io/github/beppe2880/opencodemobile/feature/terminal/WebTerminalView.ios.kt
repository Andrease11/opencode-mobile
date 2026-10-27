package io.github.beppe2880.opencodemobile.feature.terminal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.UIKit.UIColor
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

private const val HANDLER = "oc"

/**
 * The page's `OcTerminal` bridge over WebKit messages. iOS has no synchronous bridge, so the
 * font is injected before the page runs and `font()` returns it.
 */
private const val BRIDGE = """
window.OcTerminal = (function () {
  const post = (message) => window.webkit.messageHandlers.$HANDLER.postMessage(message);
  return {
    ready() { post({ t: "ready" }); },
    input(data) { post({ t: "input", d: data }); },
    resize(columns, rows) { post({ t: "resize", c: String(columns), r: String(rows) }); },
    modifiersUsed() { post({ t: "modifiersUsed" }); },
    font() { return window.__ocFont || ""; },
  };
})();
"""

/**
 * A WKWebView that only shows the bundled page. iOS opens the keyboard only after a tap on
 * the terminal, so [openKeyboard] has no effect here (the setting is hidden on iOS).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun PlatformTerminalView(surface: WebSurface, page: TerminalPage, openKeyboard: Boolean, modifier: Modifier) {
    val terminal = remember(surface) { IosTerminalView(surface) }
    UIKitView(
        factory = { terminal.create(page) },
        modifier = modifier,
        onRelease = { terminal.release() },
        properties = UIKitInteropProperties(
            // Touches go straight to xterm.js, which scrolls its history and selects text itself.
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
        ),
    )
}

@OptIn(ExperimentalForeignApi::class)
private class IosTerminalView(private val surface: WebSurface) {
    private var webView: WKWebView? = null
    private var bridgePage: WebTerminalPage? = null

    // WKWebView keeps its navigation delegate weakly.
    private val navigation = BundledPageOnly()

    fun create(page: TerminalPage): WKWebView {
        val configuration = WKWebViewConfiguration()
        val content = configuration.userContentController
        content.addUserScript(script("window.__ocFont = \"${page.fontBase64}\";"))
        content.addUserScript(script(BRIDGE))
        val view = WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = configuration)
        val calls = object : WebTerminalPage {
            override fun call(function: String, vararg arguments: String) {
                view.evaluateJavaScript("window.oc.$function(${arguments.joinToString(",")})", completionHandler = null)
            }
        }
        content.addScriptMessageHandler(Messages(surface, calls), HANDLER)
        view.setOpaque(false)
        view.backgroundColor = UIColor.clearColor
        view.scrollView.backgroundColor = UIColor.clearColor
        // xterm.js scrolls its own viewport; the page itself never moves.
        view.scrollView.scrollEnabled = false
        view.scrollView.bounces = false
        view.scrollView.contentInsetAdjustmentBehavior =
            UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
        view.navigationDelegate = navigation
        val url = NSURL.URLWithString(page.uri) ?: error("Bad terminal page URI")
        view.loadFileURL(url, allowingReadAccessToURL = url.URLByDeletingLastPathComponent ?: url)
        webView = view
        bridgePage = calls
        return view
    }

    fun release() {
        bridgePage?.let(surface::detach)
        webView?.configuration?.userContentController?.removeScriptMessageHandlerForName(HANDLER)
        webView?.stopLoading()
        webView = null
        bridgePage = null
    }

    private fun script(source: String) = WKUserScript(
        source = source,
        injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
        forMainFrameOnly = true,
    )
}

/** Calls of the page's bridge; WebKit delivers them on the main thread. */
private class Messages(private val surface: WebSurface, private val page: WebTerminalPage) :
    NSObject(),
    WKScriptMessageHandlerProtocol {
    override fun userContentController(userContentController: WKUserContentController, didReceiveScriptMessage: WKScriptMessage) {
        val body = didReceiveScriptMessage.body as? Map<*, *> ?: return
        when (body["t"]) {
            "ready" -> surface.attach(page)
            "input" -> (body["d"] as? String)?.let(surface::onInput)
            "resize" -> {
                val columns = (body["c"] as? String)?.toIntOrNull() ?: return
                val rows = (body["r"] as? String)?.toIntOrNull() ?: return
                if (columns > 0 && rows > 0) surface.onPageResize(columns, rows)
            }
            "modifiersUsed" -> surface.onPageModifiersUsed()
        }
    }
}

/** Nothing but the bundled page is ever shown. */
private class BundledPageOnly :
    NSObject(),
    WKNavigationDelegateProtocol {
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val fileUrl = decidePolicyForNavigationAction.request.URL?.isFileURL() == true
        decisionHandler(
            if (fileUrl) WKNavigationActionPolicy.WKNavigationActionPolicyAllow else WKNavigationActionPolicy.WKNavigationActionPolicyCancel,
        )
    }
}
