package com.danyastry.e2echat.core.protocol

import com.danyastry.e2echat.core.crypto.Ed25519
import com.danyastry.e2echat.core.crypto.Hkdf
import com.danyastry.e2echat.core.crypto.X25519
import java.security.SecureRandom

class InvalidSignedPreKeyException : Exception("The signed prekey is not signed by the identity key")

class UnknownPreKeyException(message: String) : Exception(message)

class InitialHeader(
    val identityKey: ByteArray,
    val ephemeralKey: ByteArray,
    val signedPreKeyId: Int,
    val oneTimePreKeyId: Int?,
) {
    val size: Int
        get() = identityKey.size + ephemeralKey.size + Int.SIZE_BYTES + if (oneTimePreKeyId != null) Int.SIZE_BYTES else 0
}

class X3dhResult(val sharedSecret: ByteArray, val associatedData: ByteArray, val dhOutputs: List<ByteArray>)

class Initiation(val header: InitialHeader, val result: X3dhResult)

class X3dhUser(
    val name: String,
    oneTimePreKeyCount: Int = DEFAULT_ONE_TIME_PREKEYS,
    private val random: SecureRandom = SecureRandom(),
) {
    val identity = IdentityKeys.generate(random)

    val signedPreKey: SignedPreKey = X25519.generate(random).let { pair ->
        SignedPreKey(id = 1, keyPair = pair, signature = Ed25519.sign(identity.signing.privateKey, pair.publicKey))
    }

    private val oneTimePreKeys = (1..oneTimePreKeyCount).associateWith { OneTimePreKey(it, X25519.generate(random)) }.toMutableMap()

    val oneTimePreKeysHeld: Int
        get() = oneTimePreKeys.size

    fun publishedKeys() = PublishedKeys(
        identityKey = identity.dh.publicKey,
        signingKey = identity.signing.publicKey,
        signedPreKeyId = signedPreKey.id,
        signedPreKey = signedPreKey.keyPair.publicKey,
        signedPreKeySignature = signedPreKey.signature,
        oneTimePreKeys = oneTimePreKeys.mapValues { it.value.keyPair.publicKey },
    )

    fun initiate(bundle: PreKeyBundle): Initiation {
        if (!Ed25519.verify(bundle.signingKey, bundle.signedPreKey, bundle.signedPreKeySignature)) throw InvalidSignedPreKeyException()
        val ephemeral = X25519.generate(random)
        val dhOutputs = listOfNotNull(
            X25519.agree(identity.dh.privateKey, bundle.signedPreKey),
            X25519.agree(ephemeral.privateKey, bundle.identityKey),
            X25519.agree(ephemeral.privateKey, bundle.signedPreKey),
            bundle.oneTimePreKey?.let { X25519.agree(ephemeral.privateKey, it) },
        )
        val header = InitialHeader(identity.dh.publicKey, ephemeral.publicKey, bundle.signedPreKeyId, bundle.oneTimePreKeyId)
        return Initiation(header, X3dhResult(X3dh.deriveSecret(dhOutputs), identity.dh.publicKey + bundle.identityKey, dhOutputs))
    }

    fun respond(header: InitialHeader): X3dhResult {
        if (header.signedPreKeyId != signedPreKey.id) throw UnknownPreKeyException("Unknown signed prekey ${header.signedPreKeyId}")
        val oneTime = header.oneTimePreKeyId?.let { id ->
            oneTimePreKeys.remove(id) ?: throw UnknownPreKeyException("One-time prekey $id is unknown or already used")
        }
        val dhOutputs = listOfNotNull(
            X25519.agree(signedPreKey.keyPair.privateKey, header.identityKey),
            X25519.agree(identity.dh.privateKey, header.ephemeralKey),
            X25519.agree(signedPreKey.keyPair.privateKey, header.ephemeralKey),
            oneTime?.let { X25519.agree(it.keyPair.privateKey, header.ephemeralKey) },
        )
        return X3dhResult(X3dh.deriveSecret(dhOutputs), header.identityKey + identity.dh.publicKey, dhOutputs)
    }

    companion object {
        const val DEFAULT_ONE_TIME_PREKEYS = 3
    }
}

object X3dh {

    const val SHARED_SECRET_BYTES = 32
    private val INFO = "E2EChat X3DH".encodeToByteArray()
    private val PREFIX = ByteArray(32) { 0xFF.toByte() }
    private val SALT = ByteArray(32)

    fun deriveSecret(dhOutputs: List<ByteArray>): ByteArray =
        Hkdf.sha256(PREFIX + dhOutputs.reduce { acc, bytes -> acc + bytes }, SALT, INFO, SHARED_SECRET_BYTES)
}
