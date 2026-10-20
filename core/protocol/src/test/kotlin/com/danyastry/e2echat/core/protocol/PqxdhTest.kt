package com.danyastry.e2echat.core.protocol

import com.danyastry.e2echat.core.crypto.MlKem
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class PqxdhTest {

    private val server = PqxdhKeyServer()
    private val user1 = PqxdhUser("User 1")
    private val user2 = PqxdhUser("User 2").also { server.publish("User 2", it.publishedKeys()) }

    @Test
    fun `both users derive the same 32 byte secret`() {
        val initiation = user1.initiate(server.fetchBundle("User 2"))

        val responder = user2.respond(initiation.header)

        assertThat(initiation.result.sharedSecret).hasLength(32)
        assertThat(responder.sharedSecret).isEqualTo(initiation.result.sharedSecret)
        assertThat(responder.associatedData).isEqualTo(initiation.result.associatedData)
    }

    @Test
    fun `the ml-kem secret is mixed in, so the result differs from plain x3dh`() {
        val result = user1.initiate(server.fetchBundle("User 2")).result

        assertThat(result.dhOutputs).hasSize(4)
        assertThat(result.sharedSecret).isEqualTo(Pqxdh.deriveSecret(result.dhOutputs, result.kemSecret))
        assertThat(result.sharedSecret).isNotEqualTo(X3dh.deriveSecret(result.dhOutputs))
    }

    @Test
    fun `knowing every dh output is not enough without the ml-kem secret`() {
        val result = user1.initiate(server.fetchBundle("User 2")).result

        val guess = Pqxdh.deriveSecret(result.dhOutputs, ByteArray(MlKem.SHARED_SECRET_BYTES))

        assertThat(guess).isNotEqualTo(result.sharedSecret)
    }

    @Test
    fun `one-time ml-kem prekeys are handed out first, then the last-resort key`() {
        val bundles = List(PqxdhUser.DEFAULT_ONE_TIME_KEM_PREKEYS + 2) { server.fetchBundle("User 2") }

        assertThat(bundles.map { it.kemPreKey.id }).containsExactly(1, 2, 3, 0, 0).inOrder()
        assertThat(bundles.map { it.isLastResort }).containsExactly(false, false, false, true, true).inOrder()
        assertThat(server.oneTimeKemPreKeysLeft("User 2")).isEqualTo(0)
    }

    @Test
    fun `user 2 deletes a one-time ml-kem prekey after using it`() {
        val header = user1.initiate(server.fetchBundle("User 2")).header

        user2.respond(header)

        assertThat(user2.oneTimeKemPreKeysHeld).isEqualTo(2)
        assertThrows(UnknownPreKeyException::class.java) { user2.respond(header) }
    }

    @Test
    fun `the last-resort ml-kem prekey keeps working after one-time keys run out`() {
        repeat(PqxdhUser.DEFAULT_ONE_TIME_KEM_PREKEYS) { user2.respond(user1.initiate(server.fetchBundle("User 2")).header) }

        repeat(2) {
            val initiation = user1.initiate(server.fetchBundle("User 2"))

            assertThat(initiation.header.kemPreKeyId).isEqualTo(PqxdhUser.LAST_RESORT_KEM_PREKEY_ID)
            assertThat(user2.respond(initiation.header).sharedSecret).isEqualTo(initiation.result.sharedSecret)
        }
    }

    @Test
    fun `a server that swaps in its own ml-kem key is caught`() {
        val bundle = server.fetchBundle("User 2")
        val serverKey = MlKem.generate().publicKey
        val swapped = PqxdhBundle(bundle.classical, SignedKemPreKey(bundle.kemPreKey.id, serverKey, bundle.kemPreKey.signature), bundle.isLastResort)

        assertThrows(InvalidKemPreKeyException::class.java) { user1.initiate(swapped) }
    }

    @Test
    fun `the signed prekey is still checked`() {
        val bundle = server.fetchBundle("User 2")
        val classical = bundle.classical
        val forged = PreKeyBundle(
            classical.identityKey, classical.signingKey, classical.signedPreKeyId, classical.signedPreKey,
            ByteArray(64), classical.oneTimePreKeyId, classical.oneTimePreKey,
        )

        assertThrows(InvalidSignedPreKeyException::class.java) { user1.initiate(PqxdhBundle(forged, bundle.kemPreKey, bundle.isLastResort)) }
    }

    @Test
    fun `a changed ml-kem ciphertext leaves user 2 with a different secret`() {
        val initiation = user1.initiate(server.fetchBundle("User 2"))
        val header = initiation.header
        val changed = header.kemCiphertext.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }

        val responder = user2.respond(PqxdhHeader(header.classical, header.kemPreKeyId, changed))

        assertThat(responder.sharedSecret).isNotEqualTo(initiation.result.sharedSecret)
    }

    @Test
    fun `the first message carries the 1568 byte ml-kem ciphertext`() {
        val initiation = user1.initiate(server.fetchBundle("User 2"))

        assertThat(initiation.header.kemCiphertext).hasLength(1568)
        assertThat(initiation.header.size).isEqualTo(initiation.header.classical.size + 4 + 1568)
        assertThat(server.fetchBundle("User 2").size).isGreaterThan(1568 + 64)
    }

    @Test
    fun `the double ratchet runs on top of the pqxdh secret`() {
        val bundle = server.fetchBundle("User 2")
        val initiation = user1.initiate(bundle)
        val responder = user2.respond(initiation.header)
        val sender = DoubleRatchet.initiator(initiation.result.sharedSecret, bundle.classical.signedPreKey, initiation.result.associatedData)
        val receiver = DoubleRatchet.responder(responder.sharedSecret, user2.signedPreKey.keyPair, responder.associatedData)

        val message = sender.encrypt("Hello after quantum".encodeToByteArray())

        assertThat(receiver.decrypt(message).decodeToString()).isEqualTo("Hello after quantum")
    }
}
