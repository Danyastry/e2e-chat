package com.danyastry.e2echat.core.protocol

import com.danyastry.e2echat.core.crypto.Ed25519
import com.danyastry.e2echat.core.crypto.Hkdf
import com.danyastry.e2echat.core.crypto.KeyPair
import com.danyastry.e2echat.core.crypto.MlKem
import java.security.SecureRandom

class InvalidKemPreKeyException : Exception("The ML-KEM prekey is not signed by the identity key")

class SignedKemPreKey(val id: Int, val publicKey: ByteArray, val signature: ByteArray) {
    val size: Int
        get() = Int.SIZE_BYTES + publicKey.size + signature.size
}

class PqxdhPublishedKeys(
    val classical: PublishedKeys,
    val lastResortKemPreKey: SignedKemPreKey,
    val oneTimeKemPreKeys: List<SignedKemPreKey>,
)

class PqxdhBundle(val classical: PreKeyBundle, val kemPreKey: SignedKemPreKey, val isLastResort: Boolean) {
    val size: Int
        get() = classical.size + kemPreKey.size
}

class PqxdhHeader(val classical: InitialHeader, val kemPreKeyId: Int, val kemCiphertext: ByteArray) {
    val size: Int
        get() = classical.size + Int.SIZE_BYTES + kemCiphertext.size
}

class PqxdhResult(val sharedSecret: ByteArray, val associatedData: ByteArray, val dhOutputs: List<ByteArray>, val kemSecret: ByteArray)

class PqxdhInitiation(val header: PqxdhHeader, val result: PqxdhResult)

class PqxdhKeyServer {

    private val classical = KeyServer()
    private val lastResort = mutableMapOf<String, SignedKemPreKey>()
    private val oneTime = mutableMapOf<String, ArrayDeque<SignedKemPreKey>>()

    fun publish(user: String, keys: PqxdhPublishedKeys) {
        classical.publish(user, keys.classical)
        lastResort[user] = keys.lastResortKemPreKey
        oneTime[user] = ArrayDeque(keys.oneTimeKemPreKeys.sortedBy { it.id })
    }

    fun fetchBundle(user: String): PqxdhBundle {
        val bundle = classical.fetchBundle(user)
        val oneTimeKey = oneTime.getValue(user).removeFirstOrNull()
        return PqxdhBundle(bundle, oneTimeKey ?: lastResort.getValue(user), isLastResort = oneTimeKey == null)
    }

    fun oneTimePreKeysLeft(user: String): Int = classical.oneTimePreKeysLeft(user)

    fun oneTimeKemPreKeysLeft(user: String): Int = oneTime[user]?.size ?: 0
}

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

object Pqxdh {

    const val KEM_KEY_TYPE: Byte = 0x08
    private val INFO = "E2EChat PQXDH".encodeToByteArray()
    private val PREFIX = ByteArray(32) { 0xFF.toByte() }
    private val SALT = ByteArray(32)

    fun encodeKemKey(publicKey: ByteArray): ByteArray = byteArrayOf(KEM_KEY_TYPE) + publicKey

    fun deriveSecret(dhOutputs: List<ByteArray>, kemSecret: ByteArray): ByteArray =
        Hkdf.sha256(PREFIX + dhOutputs.reduce { acc, bytes -> acc + bytes } + kemSecret, SALT, INFO, X3dh.SHARED_SECRET_BYTES)
}
