package com.danyastry.e2echat.core.crypto

import java.security.MessageDigest
import org.bouncycastle.crypto.InvalidCipherTextException
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.paddings.PKCS7Padding
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV

class AuthenticationException : Exception("The message was changed or encrypted with another key")

object MessageCipher {

    const val MAC_BYTES = Hmac.SHA256_BYTES
    private const val KEY_BYTES = 32
    private const val IV_BYTES = 16
    private val INFO = "E2EChat message keys".encodeToByteArray()

    fun encrypt(messageKey: ByteArray, plaintext: ByteArray, associatedData: ByteArray): ByteArray {
        val keys = MessageKeys.from(messageKey)
        val ciphertext = cbc(true, keys, plaintext)
        return ciphertext + Hmac.sha256(keys.authKey, associatedData + ciphertext)
    }

    fun decrypt(messageKey: ByteArray, sealed: ByteArray, associatedData: ByteArray): ByteArray {
        if (sealed.size < MAC_BYTES + IV_BYTES) throw AuthenticationException()
        val keys = MessageKeys.from(messageKey)
        val ciphertext = sealed.copyOfRange(0, sealed.size - MAC_BYTES)
        val mac = sealed.copyOfRange(sealed.size - MAC_BYTES, sealed.size)
        if (!MessageDigest.isEqual(mac, Hmac.sha256(keys.authKey, associatedData + ciphertext))) throw AuthenticationException()
        return try {
            cbc(false, keys, ciphertext)
        } catch (_: InvalidCipherTextException) {
            throw AuthenticationException()
        }
    }

    private fun cbc(encrypt: Boolean, keys: MessageKeys, input: ByteArray): ByteArray {
        val cipher = PaddedBufferedBlockCipher(CBCBlockCipher.newInstance(AESEngine.newInstance()), PKCS7Padding())
        cipher.init(encrypt, ParametersWithIV(KeyParameter(keys.encryptionKey), keys.iv))
        val output = ByteArray(cipher.getOutputSize(input.size))
        val length = cipher.processBytes(input, 0, input.size, output, 0)
        val total = length + cipher.doFinal(output, length)
        return output.copyOf(total)
    }

    private class MessageKeys(val encryptionKey: ByteArray, val authKey: ByteArray, val iv: ByteArray) {
        companion object {
            fun from(messageKey: ByteArray): MessageKeys {
                val derived = Hkdf.sha256(messageKey, ByteArray(KEY_BYTES), INFO, KEY_BYTES * 2 + IV_BYTES)
                return MessageKeys(
                    encryptionKey = derived.copyOfRange(0, KEY_BYTES),
                    authKey = derived.copyOfRange(KEY_BYTES, KEY_BYTES * 2),
                    iv = derived.copyOfRange(KEY_BYTES * 2, KEY_BYTES * 2 + IV_BYTES),
                )
            }
        }
    }
}
