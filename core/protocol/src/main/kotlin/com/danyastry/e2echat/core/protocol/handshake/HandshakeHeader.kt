package com.danyastry.e2echat.core.protocol.handshake

interface HandshakeHeader {
    val identityKey: ByteArray

    val ephemeralKey: ByteArray

    val size: Int
}
