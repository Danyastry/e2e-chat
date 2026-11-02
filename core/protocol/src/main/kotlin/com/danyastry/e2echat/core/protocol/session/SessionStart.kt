package com.danyastry.e2echat.core.protocol.session

import com.danyastry.e2echat.core.protocol.handshake.HandshakeHeader
import com.danyastry.e2echat.core.protocol.handshake.HandshakeResult
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet

class SessionStart<Header : HandshakeHeader>(
    val header: Header,
    val result: HandshakeResult,
    val session: DoubleRatchet,
)
