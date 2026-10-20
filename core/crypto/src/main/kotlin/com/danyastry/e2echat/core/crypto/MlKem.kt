package com.danyastry.e2echat.core.crypto

import java.security.SecureRandom
import org.bouncycastle.crypto.kems.MLKEMExtractor
import org.bouncycastle.crypto.kems.MLKEMGenerator
import org.bouncycastle.crypto.params.MLKEMParameters
import org.bouncycastle.crypto.params.MLKEMPrivateKeyParameters
import org.bouncycastle.crypto.params.MLKEMPublicKeyParameters

class Encapsulation(val ciphertext: ByteArray, val sharedSecret: ByteArray)

object MlKem {

    const val SEED_BYTES = 64
    const val PUBLIC_KEY_BYTES = 1568
    const val CIPHERTEXT_BYTES = 1568
    const val SHARED_SECRET_BYTES = 32
    private const val MESSAGE_BYTES = 32
    private val PARAMETERS = MLKEMParameters.ml_kem_1024

    fun generate(random: SecureRandom = SecureRandom()): KeyPair = fromSeed(ByteArray(SEED_BYTES).also(random::nextBytes))

    fun fromSeed(seed: ByteArray): KeyPair {
        require(seed.size == SEED_BYTES) { "ML-KEM seeds are $SEED_BYTES bytes, got ${seed.size}" }
        return KeyPair(seed.copyOf(), privateKey(seed).publicKey)
    }

    fun encapsulate(publicKey: ByteArray, random: SecureRandom = SecureRandom()): Encapsulation =
        encapsulate(publicKey, ByteArray(MESSAGE_BYTES).also(random::nextBytes))

    internal fun encapsulate(publicKey: ByteArray, message: ByteArray): Encapsulation {
        require(publicKey.size == PUBLIC_KEY_BYTES) { "ML-KEM-1024 public keys are $PUBLIC_KEY_BYTES bytes, got ${publicKey.size}" }
        val result = MLKEMGenerator.internalGenerateEncapsulated(MLKEMPublicKeyParameters(PARAMETERS, publicKey), message)
        return Encapsulation(result.encapsulation, result.secret)
    }

    fun decapsulate(privateKey: ByteArray, ciphertext: ByteArray): ByteArray {
        require(ciphertext.size == CIPHERTEXT_BYTES) { "ML-KEM-1024 ciphertexts are $CIPHERTEXT_BYTES bytes, got ${ciphertext.size}" }
        return MLKEMExtractor(privateKey(privateKey)).extractSecret(ciphertext)
    }

    internal fun expandedPrivateKey(seed: ByteArray): ByteArray = privateKey(seed).encoded

    private fun privateKey(seed: ByteArray) = MLKEMPrivateKeyParameters(PARAMETERS, seed)
}
