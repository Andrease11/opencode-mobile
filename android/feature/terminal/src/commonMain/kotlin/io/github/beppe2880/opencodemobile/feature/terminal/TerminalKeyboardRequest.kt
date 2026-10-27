package io.github.beppe2880.opencodemobile.feature.terminal

/** One automatic IME request per screen entry, preserved across configuration changes. */
internal class TerminalKeyboardRequest(handled: Boolean = false) {
    var handled = handled
        private set

    fun consumeIfReady(pageFocused: Boolean, windowFocused: Boolean, allowSoftKeyboard: Boolean = true): Boolean {
        if (handled || !pageFocused || !windowFocused) return false
        handled = true
        return allowSoftKeyboard
    }
}
