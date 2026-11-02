package com.danyastry.e2echat.core.protocol.x3dh

import com.danyastry.e2echat.core.protocol.handshake.HandshakeHeader

class InitialHeader(
    override val identityKey: ByteArray,
    override val ephemeralKey: ByteArray,
    val signedPreKeyId: Int,
    val oneTimePreKeyId: Int?,
) : HandshakeHeader {
    override val size: Int
        get() = identityKey.size + ephemeralKey.size + Int.SIZE_BYTES + if (oneTimePreKeyId != null) Int.SIZE_BYTES else 0
}
