package io.github.beppe2880.opencodemobile.feature.connect

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CleartextRiskTest {

    @Test
    fun `local networks and VPN ranges are not flagged`() {
        listOf(
            "http://localhost:4096", "http://127.0.0.1:4096", "http://10.0.2.2:4096",
            "http://192.168.1.20:4096", "http://172.20.0.5:4096", "http://169.254.1.1:4096",
            "http://100.100.12.34:4096", "http://desktop.local:4096", "http://box.tail1234.ts.net:4096",
            "http://my-box:4096", "my-box", "http://[::1]:4096", "http://[fd12::1]:4096", "",
        ).forEach { assertFalse(it, isPublicCleartext(it)) }
    }

    @Test
    fun `public hosts over plain http are flagged`() {
        listOf("http://8.8.8.8:4096", "http://oc.example.com:4096", "oc.example.com", "http://172.32.0.1:4096")
            .forEach { assertTrue(it, isPublicCleartext(it)) }
    }

    @Test
    fun `https is never flagged`() {
        assertFalse(isPublicCleartext("https://oc.example.com"))
    }
}
