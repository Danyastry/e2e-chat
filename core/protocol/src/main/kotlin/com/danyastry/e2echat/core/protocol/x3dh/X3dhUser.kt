package com.danyastry.e2echat.core.protocol.x3dh

import com.danyastry.e2echat.core.crypto.curve.Ed25519
import com.danyastry.e2echat.core.crypto.curve.X25519
import com.danyastry.e2echat.core.protocol.keys.IdentityKeys
import com.danyastry.e2echat.core.protocol.keys.OneTimePreKey
import com.danyastry.e2echat.core.protocol.keys.PreKeyBundle
import com.danyastry.e2echat.core.protocol.keys.PublishedKeys
import com.danyastry.e2echat.core.protocol.keys.SignedPreKey
import java.security.SecureRandom

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
