package com.danyastry.e2echat.core.crypto

import java.security.SecureRandom
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer

object Ed25519 {

    const val KEY_BYTES = 32
    const val SIGNATURE_BYTES = 64

    fun generate(random: SecureRandom = SecureRandom()): KeyPair {
        val privateKey = Ed25519PrivateKeyParameters(random)
        return KeyPair(privateKey.encoded, privateKey.generatePublicKey().encoded)
    }

    fun publicKeyOf(privateKey: ByteArray): ByteArray = Ed25519PrivateKeyParameters(privateKey, 0).generatePublicKey().encoded

    fun sign(privateKey: ByteArray, message: ByteArray): ByteArray = Ed25519Signer().run {
        init(true, Ed25519PrivateKeyParameters(privateKey, 0))
        update(message, 0, message.size)
        generateSignature()
    }

    fun verify(publicKey: ByteArray, message: ByteArray, signature: ByteArray): Boolean {
        if (publicKey.size != KEY_BYTES || signature.size != SIGNATURE_BYTES) return false
        return Ed25519Signer().run {
            init(false, Ed25519PublicKeyParameters(publicKey, 0))
            update(message, 0, message.size)
            verifySignature(signature)
        }
    }
}
