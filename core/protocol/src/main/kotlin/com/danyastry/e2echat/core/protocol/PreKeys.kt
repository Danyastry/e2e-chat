package com.danyastry.e2echat.core.protocol

import com.danyastry.e2echat.core.crypto.Ed25519
import com.danyastry.e2echat.core.crypto.KeyPair
import com.danyastry.e2echat.core.crypto.X25519
import java.security.SecureRandom

class IdentityKeys(val dh: KeyPair, val signing: KeyPair) {
    companion object {
        fun generate(random: SecureRandom = SecureRandom()) = IdentityKeys(X25519.generate(random), Ed25519.generate(random))
    }
}

class SignedPreKey(val id: Int, val keyPair: KeyPair, val signature: ByteArray)

class OneTimePreKey(val id: Int, val keyPair: KeyPair)

class PreKeyBundle(
    val identityKey: ByteArray,
    val signingKey: ByteArray,
    val signedPreKeyId: Int,
    val signedPreKey: ByteArray,
    val signedPreKeySignature: ByteArray,
    val oneTimePreKeyId: Int?,
    val oneTimePreKey: ByteArray?,
)

class PublishedKeys(
    val identityKey: ByteArray,
    val signingKey: ByteArray,
    val signedPreKeyId: Int,
    val signedPreKey: ByteArray,
    val signedPreKeySignature: ByteArray,
    val oneTimePreKeys: Map<Int, ByteArray>,
)

class KeyServer {

    private val published = mutableMapOf<String, PublishedKeys>()
    private val remaining = mutableMapOf<String, ArrayDeque<Int>>()

    fun publish(user: String, keys: PublishedKeys) {
        published[user] = keys
        remaining[user] = ArrayDeque(keys.oneTimePreKeys.keys.sorted())
    }

    fun fetchBundle(user: String): PreKeyBundle {
        val keys = requireNotNull(published[user]) { "$user has not published any keys" }
        val oneTimeId = remaining.getValue(user).removeFirstOrNull()
        return PreKeyBundle(
            identityKey = keys.identityKey,
            signingKey = keys.signingKey,
            signedPreKeyId = keys.signedPreKeyId,
            signedPreKey = keys.signedPreKey,
            signedPreKeySignature = keys.signedPreKeySignature,
            oneTimePreKeyId = oneTimeId,
            oneTimePreKey = oneTimeId?.let { keys.oneTimePreKeys.getValue(it) },
        )
    }

    fun oneTimePreKeysLeft(user: String): Int = remaining[user]?.size ?: 0
}
