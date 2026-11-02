package com.danyastry.e2echat.core.protocol.session

import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet

class SessionStart<Header, Handshake>(
    val header: Header,
    val handshake: Handshake,
    val session: DoubleRatchet,
)
