package io.github.beppe2880.opencodemobile.core.network

import io.github.beppe2880.opencodemobile.core.domain.ConnectTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConnectLinkParserTest {

    @Test
    fun `pairing link from a QR code`() {
        assertEquals(
            ConnectTarget.Pairing("http://192.168.1.20:4096", "AbC123"),
            ConnectLinkParser.parse("  http://192.168.1.20:4096/auth/connect/AbC123  "),
        )
    }

    @Test
    fun `opencode pair QR code keeps every address with loopback last`() {
        val qr = """{"code":"AbC-123_x","urls":["http://127.0.0.1:4096","http://192.168.1.20:4096","http://172.17.0.1:4096/","http://192.168.1.20:4096"]}"""
        assertEquals(
            ConnectTarget.Pairing(
                "http://192.168.1.20:4096",
                "AbC-123_x",
                listOf("http://172.17.0.1:4096", "http://127.0.0.1:4096"),
            ),
            ConnectLinkParser.parse(qr),
        )
    }

    @Test
    fun `opencode pair QR code without code or addresses is rejected`() {
        assertNull(ConnectLinkParser.parse("""{"code":"","urls":["http://host:4096"]}"""))
        assertNull(ConnectLinkParser.parse("""{"code":"abc","urls":[]}"""))
        assertNull(ConnectLinkParser.parse("""{"code":"abc","urls":["not a url"]}"""))
        assertNull(ConnectLinkParser.parse("""{"code":"abc""""))
    }

    @Test
    fun `pairing link without code is rejected`() {
        assertNull(ConnectLinkParser.parse("http://host:4096/auth/connect/"))
    }

    @Test
    fun `bare host gets http and the default port`() {
        assertEquals(ConnectTarget.Url("http://my-box:4096"), ConnectLinkParser.parse("my-box"))
        assertEquals(ConnectTarget.Url("http://10.0.0.5:4096"), ConnectLinkParser.parse("10.0.0.5/"))
    }

    @Test
    fun `explicit port and scheme are kept`() {
        assertEquals(ConnectTarget.Url("http://my-box:8080"), ConnectLinkParser.parse("my-box:8080"))
        assertEquals(ConnectTarget.Url("https://oc.example.com"), ConnectLinkParser.parse("https://oc.example.com/"))
        assertEquals(ConnectTarget.Url("https://oc.example.com:4096"), ConnectLinkParser.parse("https://oc.example.com:4096/api"))
    }

    @Test
    fun `ipv6 hosts keep brackets`() {
        assertEquals(ConnectTarget.Url("http://[::1]:4096"), ConnectLinkParser.parse("[::1]"))
        assertEquals(ConnectTarget.Url("http://[::1]:9000"), ConnectLinkParser.parse("[::1]:9000"))
    }

    @Test
    fun `garbage is rejected`() {
        assertNull(ConnectLinkParser.parse(""))
        assertNull(ConnectLinkParser.parse("not a url"))
        assertNull(ConnectLinkParser.parse("ftp://host"))
    }

    @Test
    fun `normalizeBaseUrl drops pairing paths`() {
        assertEquals("http://host:4096", ConnectLinkParser.normalizeBaseUrl("http://host:4096/auth/connect/xyz"))
    }

    @Test
    fun `hosts are normalized like OkHttp`() {
        assertEquals(ConnectTarget.Url("http://xn--bcher-kva.de:4096"), ConnectLinkParser.parse("bücher.de"))
        assertEquals(ConnectTarget.Url("http://my-box:4096"), ConnectLinkParser.parse("MY-Box"))
        assertEquals(ConnectTarget.Url("http://my-box:4096"), ConnectLinkParser.parse("http://my%2Dbox:4096"))
        assertEquals(ConnectTarget.Url("http://host"), ConnectLinkParser.parse("http://host:+80"))
        assertEquals(ConnectTarget.Pairing("http://host:4096", "abc"), ConnectLinkParser.parse("http://host:4096\\auth\\connect\\abc"))
    }

    @Test
    fun `ipv6 hosts are canonical and mapped ipv4 becomes ipv4`() {
        assertEquals(ConnectTarget.Url("http://[::1]:4096"), ConnectLinkParser.parse("[0:0:0:0:0:0:0:1]"))
        assertEquals(ConnectTarget.Url("http://[2001:db8::1]:4096"), ConnectLinkParser.parse("[2001:DB8:0:0:0:0:0:1]"))
        assertEquals(ConnectTarget.Url("http://1.2.3.4:4096"), ConnectLinkParser.parse("[::ffff:1.2.3.4]"))
        assertNull(ConnectLinkParser.parse("http://[fe80::1%25wlan0]:4096"))
        assertNull(ConnectLinkParser.parse("http://[1:2:3]:4096"))
    }

    @Test
    fun `loopback in any ipv6 form goes last in pair codes`() {
        val qr = """{"code":"c","urls":["http://[0:0:0:0:0:0:0:1]:4096","http://192.168.1.20:4096"]}"""
        assertEquals(ConnectTarget.Pairing("http://192.168.1.20:4096", "c", listOf("http://[::1]:4096")), ConnectLinkParser.parse(qr))
    }
}
