package com.danyastry.e2echat.core.protocol.handshake

import java.security.SecureRandom

interface KeyDirectory<Bundle : HandshakeBundle, Header : HandshakeHeader> {
    fun register(user: String, random: SecureRandom = SecureRandom()): Handshake<Bundle, Header>

    fun fetchBundle(user: String): Bundle
}
