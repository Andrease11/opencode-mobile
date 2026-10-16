package io.github.beppe2880.opencodemobile.core.ui.text

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class FormattingTest {

    private val locale = Locale.getDefault()

    @After
    fun restoreLocale() = Locale.setDefault(locale)

    @Test
    fun `durations stay compact`() {
        assertEquals("850ms", formatDuration(850))
        assertEquals("0ms", formatDuration(-5))
        assertEquals("3s", formatDuration(3_400))
        assertEquals("2m", formatDuration(120_000))
        assertEquals("1m 20s", formatDuration(80_000))
        assertEquals("2h 5m", formatDuration(7_500_000))
    }

    @Test
    fun `costs show cents or four decimals and nothing when free`() {
        assertEquals("", formatCost(0.0))
        assertEquals("$0.0042", formatCost(0.0042))
        assertEquals("$1.23", formatCost(1.234))
    }

    @Test
    fun `token counts follow the TUI`() {
        Locale.setDefault(Locale.ITALY)
        assertEquals("19.952 tok", formatTokenTotal(19_952))
        Locale.setDefault(Locale.US)
        assertEquals("19,952 tok", formatTokenTotal(19_952))
        assertEquals("81.5 tok/s", formatTokensPerSecond(81.46))
        assertEquals("999", formatTokens(999))
        assertEquals("1.2k", formatTokens(1_234))
        assertEquals("35k", formatTokens(35_900))
        assertEquals("1.4M", formatTokens(1_400_000))
    }
}
