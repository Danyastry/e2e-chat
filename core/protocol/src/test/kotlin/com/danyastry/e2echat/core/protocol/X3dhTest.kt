package com.danyastry.e2echat.core.protocol

import com.danyastry.e2echat.core.crypto.Hkdf
import com.danyastry.e2echat.core.crypto.X25519
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class X3dhTest {

    private val server = KeyServer()
    private val user1 = X3dhUser("User 1")
    private val user2 = X3dhUser("User 2").also { server.publish("User 2", it.publishedKeys()) }

    @Test
    fun `both users derive the same 32 byte secret`() {
        val initiation = user1.initiate(server.fetchBundle("User 2"))

        val responder = user2.respond(initiation.header)

        assertThat(initiation.result.sharedSecret).hasLength(32)
        assertThat(responder.sharedSecret).isEqualTo(initiation.result.sharedSecret)
    }

    @Test
    fun `uses four dh outputs while one-time prekeys last`() {
        val initiation = user1.initiate(server.fetchBundle("User 2"))

        assertThat(initiation.result.dhOutputs).hasSize(4)
        assertThat(user2.respond(initiation.header).dhOutputs.map { it.toList() }).isEqualTo(initiation.result.dhOutputs.map { it.toList() })
    }

    @Test
    fun `falls back to three dh outputs when one-time prekeys run out`() {
        repeat(X3dhUser.DEFAULT_ONE_TIME_PREKEYS) { user2.respond(user1.initiate(server.fetchBundle("User 2")).header) }

        val bundle = server.fetchBundle("User 2")
        val initiation = user1.initiate(bundle)

        assertThat(bundle.oneTimePreKeyId).isNull()
        assertThat(initiation.result.dhOutputs).hasSize(3)
        assertThat(user2.respond(initiation.header).sharedSecret).isEqualTo(initiation.result.sharedSecret)
    }

    @Test
    fun `the server hands out every one-time prekey only once`() {
        val ids = List(X3dhUser.DEFAULT_ONE_TIME_PREKEYS) { server.fetchBundle("User 2").oneTimePreKeyId }

        assertThat(ids).containsExactly(1, 2, 3).inOrder()
        assertThat(server.oneTimePreKeysLeft("User 2")).isEqualTo(0)
    }

    @Test
    fun `user 2 deletes a one-time prekey after using it`() {
        val header = user1.initiate(server.fetchBundle("User 2")).header

        user2.respond(header)

        assertThat(user2.oneTimePreKeysHeld).isEqualTo(2)
        assertThrows(UnknownPreKeyException::class.java) { user2.respond(header) }
    }

    @Test
    fun `refuses a signed prekey that the identity key did not sign`() {
        val real = server.fetchBundle("User 2")
        val forged = PreKeyBundle(
            identityKey = real.identityKey,
            signingKey = real.signingKey,
            signedPreKeyId = real.signedPreKeyId,
            signedPreKey = X25519.generate().publicKey,
            signedPreKeySignature = real.signedPreKeySignature,
            oneTimePreKeyId = real.oneTimePreKeyId,
            oneTimePreKey = real.oneTimePreKey,
        )

        assertThrows(InvalidSignedPreKeyException::class.java) { user1.initiate(forged) }
    }

    @Test
    fun `associated data is the initiator identity key followed by the responder identity key`() {
        val initiation = user1.initiate(server.fetchBundle("User 2"))

        assertThat(initiation.result.associatedData).isEqualTo(user1.identity.dh.publicKey + user2.identity.dh.publicKey)
        assertThat(user2.respond(initiation.header).associatedData).isEqualTo(initiation.result.associatedData)
    }

    @Test
    fun `the secret follows the spec formula`() {
        val initiation = user1.initiate(server.fetchBundle("User 2"))
        val concatenated = ByteArray(32) { 0xFF.toByte() } + initiation.result.dhOutputs.reduce { acc, bytes -> acc + bytes }

        val expected = Hkdf.sha256(concatenated, ByteArray(32), "E2EChat X3DH".encodeToByteArray(), 32)

        assertThat(initiation.result.sharedSecret).isEqualTo(expected)
    }

    @Test
    fun `every handshake makes a new secret`() {
        val first = user1.initiate(server.fetchBundle("User 2"))
        val second = user1.initiate(server.fetchBundle("User 2"))

        assertThat(first.result.sharedSecret).isNotEqualTo(second.result.sharedSecret)
    }

    @Test
    fun `a different initiator gets a different secret`() {
        val stranger = X3dhUser("Stranger")
        val bundle = server.fetchBundle("User 2")

        assertThat(stranger.initiate(bundle).result.sharedSecret).isNotEqualTo(user1.initiate(bundle).result.sharedSecret)
    }

    @Test
    fun `the initial header is small`() {
        val header = user1.initiate(server.fetchBundle("User 2")).header

        assertThat(header.size).isEqualTo(32 + 32 + 4 + 4)
    }

    @Test
    fun `fetching keys of an unknown user fails`() {
        assertThrows(IllegalArgumentException::class.java) { server.fetchBundle("Nobody") }
    }
}
