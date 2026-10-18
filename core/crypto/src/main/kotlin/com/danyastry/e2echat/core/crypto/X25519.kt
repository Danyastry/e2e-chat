package com.danyastry.e2echat.core.crypto

import java.security.SecureRandom
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters

class KeyPair(val privateKey: ByteArray, val publicKey: ByteArray)

object X25519 {

    const val KEY_BYTES = 32

    fun generate(random: SecureRandom = SecureRandom()): KeyPair {
        val privateKey = X25519PrivateKeyParameters(random)
        return KeyPair(privateKey.encoded, privateKey.generatePublicKey().encoded)
    }

    fun publicKeyOf(privateKey: ByteArray): ByteArray = X25519PrivateKeyParameters(privateKey, 0).generatePublicKey().encoded

    fun agree(privateKey: ByteArray, peerPublicKey: ByteArray): ByteArray {
        require(peerPublicKey.size == KEY_BYTES) { "X25519 public keys are $KEY_BYTES bytes, got ${peerPublicKey.size}" }
        return ByteArray(KEY_BYTES).also { secret ->
            X25519Agreement().apply { init(X25519PrivateKeyParameters(privateKey, 0)) }
                .calculateAgreement(X25519PublicKeyParameters(peerPublicKey, 0), secret, 0)
        }
    }
}
