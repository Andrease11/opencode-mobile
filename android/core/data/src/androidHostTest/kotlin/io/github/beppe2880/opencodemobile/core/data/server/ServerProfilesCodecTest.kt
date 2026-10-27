package io.github.beppe2880.opencodemobile.core.data.server

import io.github.beppe2880.opencodemobile.core.domain.CredentialKind
import io.github.beppe2880.opencodemobile.core.domain.ServerProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class ServerProfilesCodecTest {

    @Test
    fun `round trips profiles`() {
        val profiles = listOf(
            ServerProfile("1", "Desk", "http://desk:4096", secret = "pw", credential = CredentialKind.Password, addedAt = 1),
            ServerProfile("2", "Lap", "https://lap", secret = "tok", credential = CredentialKind.PairingToken, addedAt = 2),
        )
        assertEquals(profiles, ServerProfilesCodec.decode(ServerProfilesCodec.encode(profiles)))
    }

    @Test
    fun `corrupt or missing data yields no profiles`() {
        assertEquals(emptyList<ServerProfile>(), ServerProfilesCodec.decode(null))
        assertEquals(emptyList<ServerProfile>(), ServerProfilesCodec.decode("{not json"))
        assertEquals(emptyList<ServerProfile>(), ServerProfilesCodec.decode("""{"id":"1"}"""))
    }

    @Test
    fun `entries with an unknown credential kind are dropped`() {
        val raw = """[{"id":"1","name":"n","baseUrl":"http://h:4096","username":"opencode","secret":"s","credential":"Biometric","addedAt":1}]"""
        assertEquals(emptyList<ServerProfile>(), ServerProfilesCodec.decode(raw))
    }
}
