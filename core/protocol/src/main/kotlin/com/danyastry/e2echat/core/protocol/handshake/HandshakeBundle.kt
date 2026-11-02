package com.danyastry.e2echat.core.protocol.handshake

interface HandshakeBundle {
    val signedPreKey: ByteArray

    val size: Int
}
