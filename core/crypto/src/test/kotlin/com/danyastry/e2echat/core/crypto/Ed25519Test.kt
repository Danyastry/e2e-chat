package com.danyastry.e2echat.core.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Ed25519Test {

    private val secret = hex("9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60")
    private val public = hex("d75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a")
    private val emptySignature = hex(
        "e5564300c360ac729086e2cc806e828a84877f1eb8e5d974d873e065224901555fb8821590a33bacc61e39701cf9b46bd25bf5f0595bbe24655141438e7a100b",
    )

    @Test
    fun `derives the rfc 8032 public key`() {
        assertThat(Ed25519.publicKeyOf(secret)).isEqualTo(public)
    }

    @Test
    fun `signs the empty message like rfc 8032 test 1`() {
        assertThat(Ed25519.sign(secret, ByteArray(0))).isEqualTo(emptySignature)
    }

    @Test
    fun `verifies the rfc 8032 signature`() {
        assertThat(Ed25519.verify(public, ByteArray(0), emptySignature)).isTrue()
    }

    @Test
    fun `rejects a changed message or signature`() {
        val pair = Ed25519.generate()
        val signature = Ed25519.sign(pair.privateKey, "prekey".encodeToByteArray())

        assertThat(Ed25519.verify(pair.publicKey, "prekey!".encodeToByteArray(), signature)).isFalse()
        assertThat(Ed25519.verify(pair.publicKey, "prekey".encodeToByteArray(), signature.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() })).isFalse()
    }

    @Test
    fun `rejects keys and signatures of the wrong size instead of crashing`() {
        assertThat(Ed25519.verify(ByteArray(31), ByteArray(0), emptySignature)).isFalse()
        assertThat(Ed25519.verify(public, ByteArray(0), ByteArray(63))).isFalse()
    }
}
