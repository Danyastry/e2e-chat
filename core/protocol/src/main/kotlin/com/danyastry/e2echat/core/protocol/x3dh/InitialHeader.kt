package com.danyastry.e2echat.core.protocol.x3dh

class InitialHeader(
    val identityKey: ByteArray,
    val ephemeralKey: ByteArray,
    val signedPreKeyId: Int,
    val oneTimePreKeyId: Int?,
) {
    val size: Int
        get() = identityKey.size + ephemeralKey.size + Int.SIZE_BYTES + if (oneTimePreKeyId != null) Int.SIZE_BYTES else 0
}
