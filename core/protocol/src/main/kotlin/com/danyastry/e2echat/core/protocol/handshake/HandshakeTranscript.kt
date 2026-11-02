package com.danyastry.e2echat.core.protocol.handshake

class HandshakeTranscript(
    val dhOutputs: List<ByteArray>,
    val kemSecret: ByteArray?,
)
