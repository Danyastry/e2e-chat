package com.danyastry.e2echat.core.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class X25519Test {

    private val user1Private = hex("77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a")
    private val user1Public = hex("8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a")
    private val user2Private = hex("5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb")
    private val user2Public = hex("de9edb7d7b7dc1b4d35b61c2ece435373f8343c85b78674dadfc7e146f882b4f")

    @Test
    fun `derives the rfc 7748 public keys`() {
        assertThat(X25519.publicKeyOf(user1Private)).isEqualTo(user1Public)
        assertThat(X25519.publicKeyOf(user2Private)).isEqualTo(user2Public)
    }

    @Test
    fun `agrees on the rfc 7748 shared secret from both sides`() {
        val expected = "4a5d9d5ba4ce2de1728e3bf480350f25e07e21c947d19e3376f09b3c1e161742"

        assertThat(X25519.agree(user1Private, user2Public).toHex()).isEqualTo(expected)
        assertThat(X25519.agree(user2Private, user1Public).toHex()).isEqualTo(expected)
    }

    @Test
    fun `fresh key pairs agree with each other`() {
        val user1 = X25519.generate()
        val user2 = X25519.generate()

        assertThat(X25519.agree(user1.privateKey, user2.publicKey)).isEqualTo(X25519.agree(user2.privateKey, user1.publicKey))
        assertThat(user1.publicKey).hasLength(32)
    }

    @Test
    fun `rejects a public key of the wrong size`() {
        assertThrows(IllegalArgumentException::class.java) { X25519.agree(user1Private, ByteArray(31)) }
    }
}
