package com.danyastry.e2echat.core.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class MessageCipherTest {

    private val key = ByteArray(32) { it.toByte() }
    private val ad = "header".encodeToByteArray()
    private val message = "Meet me at the old bridge at 9:45".encodeToByteArray()

    @Test
    fun `decrypts what it encrypted`() {
        val sealed = MessageCipher.encrypt(key, message, ad)

        assertThat(MessageCipher.decrypt(key, sealed, ad)).isEqualTo(message)
    }

    @Test
    fun `output is padded aes blocks plus a 32 byte mac`() {
        assertThat(MessageCipher.encrypt(key, ByteArray(0), ad)).hasLength(16 + 32)
        assertThat(MessageCipher.encrypt(key, ByteArray(16), ad)).hasLength(32 + 32)
        assertThat(MessageCipher.encrypt(key, message, ad)).hasLength(48 + 32)
    }

    @Test
    fun `the same key and message always give the same output`() {
        assertThat(MessageCipher.encrypt(key, message, ad)).isEqualTo(MessageCipher.encrypt(key, message, ad))
    }

    @Test
    fun `a changed ciphertext byte is rejected`() {
        val sealed = MessageCipher.encrypt(key, message, ad).also { it[0] = (it[0].toInt() xor 1).toByte() }

        assertThrows(AuthenticationException::class.java) { MessageCipher.decrypt(key, sealed, ad) }
    }

    @Test
    fun `a changed mac is rejected`() {
        val sealed = MessageCipher.encrypt(key, message, ad).also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }

        assertThrows(AuthenticationException::class.java) { MessageCipher.decrypt(key, sealed, ad) }
    }

    @Test
    fun `changed associated data is rejected`() {
        val sealed = MessageCipher.encrypt(key, message, ad)

        assertThrows(AuthenticationException::class.java) { MessageCipher.decrypt(key, sealed, "other header".encodeToByteArray()) }
    }

    @Test
    fun `another key is rejected`() {
        val sealed = MessageCipher.encrypt(key, message, ad)

        assertThrows(AuthenticationException::class.java) { MessageCipher.decrypt(ByteArray(32), sealed, ad) }
    }

    @Test
    fun `input too short to hold a mac is rejected`() {
        assertThrows(AuthenticationException::class.java) { MessageCipher.decrypt(key, ByteArray(20), ad) }
    }
}
