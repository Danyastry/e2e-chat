package com.danyastry.e2echat.core.protocol.handshake

class HandshakeResult(
    val sharedSecret: ByteArray,
    val associatedData: ByteArray,
    val transcript: HandshakeTranscript,
)
