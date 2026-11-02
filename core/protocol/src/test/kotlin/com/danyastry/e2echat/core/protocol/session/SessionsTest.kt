package com.danyastry.e2echat.core.protocol.session

import com.danyastry.e2echat.core.crypto.cipher.AuthenticationException
import com.danyastry.e2echat.core.protocol.keys.KeyServer
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhKeyServer
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhUser
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet
import com.danyastry.e2echat.core.protocol.x3dh.X3dhUser
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class SessionsTest {

    @Test
    fun `an x3dh session start gives both sides a working ratchet`() {
        val server = KeyServer()
        val user2 = X3dhUser("User 2").also { server.publish("User 2", it.publishedKeys()) }

        val start = X3dhUser("User 1").startSession(server.fetchBundle("User 2"))
        val accepted = user2.acceptSession(start.header)

        assertThat(accepted.decrypt(start.session.encrypt(text("Hello"))).decodeToString()).isEqualTo("Hello")
        assertThat(start.session.decrypt(accepted.encrypt(text("Hi back"))).decodeToString()).isEqualTo("Hi back")
        assertThat(start.handshake.dhOutputs).hasSize(4)
    }

    @Test
    fun `a pqxdh session start gives both sides a working ratchet`() {
        val server = PqxdhKeyServer()
        val user2 = PqxdhUser("User 2").also { server.publish("User 2", it.publishedKeys()) }

        val start = PqxdhUser("User 1").startSession(server.fetchBundle("User 2"))
        val accepted = user2.acceptSession(start.header)

        assertThat(accepted.decrypt(start.session.encrypt(text("Hello after quantum"))).decodeToString()).isEqualTo("Hello after quantum")
        assertThat(start.session.decrypt(accepted.encrypt(text("Still private"))).decodeToString()).isEqualTo("Still private")
        assertThat(start.header.kemCiphertext).isNotEmpty()
    }

    @Test
    fun `a recovered secret only opens the session when it is the real one`() {
        val server = KeyServer()
        val user2 = X3dhUser("User 2").also { server.publish("User 2", it.publishedKeys()) }
        val start = X3dhUser("User 1").startSession(server.fetchBundle("User 2"))
        val message = start.session.encrypt(text("Recorded"))

        val withRealSecret = DoubleRatchet.responderFromRecoveredSecret(start.handshake.sharedSecret, user2.signedPreKey.keyPair, start.handshake.associatedData)
        val withWrongSecret = DoubleRatchet.responderFromRecoveredSecret(ByteArray(32), user2.signedPreKey.keyPair, start.handshake.associatedData)

        assertThat(withRealSecret.decrypt(message).decodeToString()).isEqualTo("Recorded")
        assertThrows(AuthenticationException::class.java) { withWrongSecret.decrypt(message) }
    }

    private fun text(value: String) = value.encodeToByteArray()
}
