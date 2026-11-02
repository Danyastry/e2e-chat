package com.danyastry.e2echat.core.protocol.keys

class PreKeyBundle(
    val identityKey: ByteArray,
    val signingKey: ByteArray,
    val signedPreKeyId: Int,
    val signedPreKey: ByteArray,
    val signedPreKeySignature: ByteArray,
    val oneTimePreKeyId: Int?,
    val oneTimePreKey: ByteArray?,
) {
    val size: Int
        get() = identityKey.size + signingKey.size + Int.SIZE_BYTES + signedPreKey.size + signedPreKeySignature.size +
            (oneTimePreKey?.let { it.size + Int.SIZE_BYTES } ?: 0)
}
