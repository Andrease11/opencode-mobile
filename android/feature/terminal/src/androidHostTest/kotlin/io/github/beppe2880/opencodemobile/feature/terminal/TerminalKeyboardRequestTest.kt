package io.github.beppe2880.opencodemobile.feature.terminal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalKeyboardRequestTest {
    @Test
    fun `waits for both the page input and the window to have focus`() {
        val request = TerminalKeyboardRequest()

        assertFalse(request.consumeIfReady(pageFocused = false, windowFocused = true))
        assertFalse(request.consumeIfReady(pageFocused = true, windowFocused = false))
        assertFalse(request.handled)
        assertTrue(request.consumeIfReady(pageFocused = true, windowFocused = true))
    }

    @Test
    fun `does not reopen the keyboard after the initial request`() {
        val request = TerminalKeyboardRequest()

        assertTrue(request.consumeIfReady(pageFocused = true, windowFocused = true))
        assertFalse(request.consumeIfReady(pageFocused = true, windowFocused = false))
        assertFalse(request.consumeIfReady(pageFocused = true, windowFocused = true))
    }

    @Test
    fun `hardware keyboard preference suppresses the IME without a deferred request`() {
        val request = TerminalKeyboardRequest()

        assertFalse(request.consumeIfReady(pageFocused = true, windowFocused = true, allowSoftKeyboard = false))
        assertTrue(request.handled)
        assertFalse(request.consumeIfReady(pageFocused = true, windowFocused = true, allowSoftKeyboard = true))
    }

    @Test
    fun `allowing the soft keyboard with hardware still waits for focus`() {
        val request = TerminalKeyboardRequest()

        assertFalse(request.consumeIfReady(pageFocused = true, windowFocused = false, allowSoftKeyboard = true))
        assertFalse(request.handled)
        assertTrue(request.consumeIfReady(pageFocused = true, windowFocused = true, allowSoftKeyboard = true))
    }

    @Test
    fun `configuration recreation preserves a pending request`() {
        val request = TerminalKeyboardRequest()
        request.consumeIfReady(pageFocused = false, windowFocused = true)
        val restored = TerminalKeyboardRequest(request.handled)

        assertTrue(restored.consumeIfReady(pageFocused = true, windowFocused = true))
    }

    @Test
    fun `configuration recreation preserves a handled request`() {
        val original = TerminalKeyboardRequest()
        original.consumeIfReady(pageFocused = true, windowFocused = true)
        val restored = TerminalKeyboardRequest(original.handled)

        assertFalse(restored.consumeIfReady(pageFocused = true, windowFocused = true))
    }

    @Test
    fun `leaving and entering the screen creates a fresh request`() {
        val previous = TerminalKeyboardRequest()
        previous.consumeIfReady(pageFocused = true, windowFocused = true)

        assertTrue(TerminalKeyboardRequest().consumeIfReady(pageFocused = true, windowFocused = true))
    }
}
