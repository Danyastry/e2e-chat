package com.danyastry.e2echat.core.protocol.pqxdh

import com.danyastry.e2echat.core.crypto.curve.Ed25519
import com.danyastry.e2echat.core.crypto.curve.KeyPair
import com.danyastry.e2echat.core.crypto.kem.MlKem
import com.danyastry.e2echat.core.protocol.keys.IdentityKeys
import com.danyastry.e2echat.core.protocol.keys.SignedPreKey
import com.danyastry.e2echat.core.protocol.x3dh.UnknownPreKeyException
import com.danyastry.e2echat.core.protocol.x3dh.X3dhUser
import java.security.SecureRandom

class PqxdhUser(
    val name: String,
    oneTimePreKeyCount: Int = X3dhUser.DEFAULT_ONE_TIME_PREKEYS,
    oneTimeKemPreKeyCount: Int = DEFAULT_ONE_TIME_KEM_PREKEYS,
    private val random: SecureRandom = SecureRandom(),
) {
    private val x3dh = X3dhUser(name, oneTimePreKeyCount, random)

    val identity: IdentityKeys
        get() = x3dh.identity

    val signedPreKey: SignedPreKey
        get() = x3dh.signedPreKey

    private val lastResortKemPreKey = MlKem.generate(random)

    private val oneTimeKemPreKeys = (1..oneTimeKemPreKeyCount).associateWith { MlKem.generate(random) }.toMutableMap()

    val oneTimeKemPreKeysHeld: Int
        get() = oneTimeKemPreKeys.size

    fun publishedKeys() = PqxdhPublishedKeys(
        classical = x3dh.publishedKeys(),
        lastResortKemPreKey = sign(LAST_RESORT_KEM_PREKEY_ID, lastResortKemPreKey),
        oneTimeKemPreKeys = oneTimeKemPreKeys.map { (id, pair) -> sign(id, pair) },
    )

    fun initiate(bundle: PqxdhBundle): PqxdhInitiation {
        val kemPreKey = bundle.kemPreKey
        if (!Ed25519.verify(bundle.classical.signingKey, Pqxdh.encodeKemKey(kemPreKey.publicKey), kemPreKey.signature)) {
            throw InvalidKemPreKeyException()
        }
        val classical = x3dh.initiate(bundle.classical)
        val encapsulation = MlKem.encapsulate(kemPreKey.publicKey, random)
        val header = PqxdhHeader(classical.header, kemPreKey.id, encapsulation.ciphertext)
        val dhOutputs = classical.result.dhOutputs
        val secret = Pqxdh.deriveSecret(dhOutputs, encapsulation.sharedSecret)
        return PqxdhInitiation(header, PqxdhResult(secret, classical.result.associatedData, dhOutputs, encapsulation.sharedSecret))
    }

    fun respond(header: PqxdhHeader): PqxdhResult {
        val kemPreKey = when (val id = header.kemPreKeyId) {
            LAST_RESORT_KEM_PREKEY_ID -> lastResortKemPreKey
            else -> oneTimeKemPreKeys[id] ?: throw UnknownPreKeyException("One-time ML-KEM prekey $id is unknown or already used")
        }
        val classical = x3dh.respond(header.classical)
        oneTimeKemPreKeys.remove(header.kemPreKeyId)
        val kemSecret = MlKem.decapsulate(kemPreKey.privateKey, header.kemCiphertext)
        return PqxdhResult(Pqxdh.deriveSecret(classical.dhOutputs, kemSecret), classical.associatedData, classical.dhOutputs, kemSecret)
    }

    private fun sign(id: Int, pair: KeyPair) =
        SignedKemPreKey(id, pair.publicKey, Ed25519.sign(identity.signing.privateKey, Pqxdh.encodeKemKey(pair.publicKey)))

    companion object {
        const val DEFAULT_ONE_TIME_KEM_PREKEYS = 3
        const val LAST_RESORT_KEM_PREKEY_ID = 0
    }
}
