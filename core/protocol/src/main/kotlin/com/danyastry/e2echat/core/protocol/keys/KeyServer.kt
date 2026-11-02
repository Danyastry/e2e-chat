package com.danyastry.e2echat.core.protocol.keys

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
