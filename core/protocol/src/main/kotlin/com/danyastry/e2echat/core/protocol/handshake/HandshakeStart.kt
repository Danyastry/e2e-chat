package com.danyastry.e2echat.core.protocol.handshake

class HandshakeStart<Header : HandshakeHeader>(
    val header: Header,
    val result: HandshakeResult,
)
