package com.danyastry.e2echat.core.protocol.pqxdh

import com.danyastry.e2echat.core.protocol.handshake.HandshakeHeader
import com.danyastry.e2echat.core.protocol.x3dh.InitialHeader

class PqxdhHeader(val classical: InitialHeader, val kemPreKeyId: Int, val kemCiphertext: ByteArray) : HandshakeHeader {
    override val identityKey: ByteArray
        get() = classical.identityKey

    override val ephemeralKey: ByteArray
        get() = classical.ephemeralKey

    override val size: Int
        get() = classical.size + Int.SIZE_BYTES + kemCiphertext.size
}
