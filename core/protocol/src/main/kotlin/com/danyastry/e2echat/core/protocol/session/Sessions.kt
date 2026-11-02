package com.danyastry.e2echat.core.protocol.session

import com.danyastry.e2echat.core.protocol.handshake.Handshake
import com.danyastry.e2echat.core.protocol.handshake.HandshakeBundle
import com.danyastry.e2echat.core.protocol.handshake.HandshakeHeader
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet
import java.security.SecureRandom

fun <Bundle : HandshakeBundle, Header : HandshakeHeader> Handshake<Bundle, Header>.startSession(
    bundle: Bundle,
    random: SecureRandom = SecureRandom(),
): SessionStart<Header> {
    val start = initiate(bundle)
    val session = DoubleRatchet.initiator(start.result.sharedSecret, bundle.signedPreKey, start.result.associatedData, random)
    return SessionStart(start.header, start.result, session)
}

fun <Bundle : HandshakeBundle, Header : HandshakeHeader> Handshake<Bundle, Header>.acceptSession(
    header: Header,
    random: SecureRandom = SecureRandom(),
): DoubleRatchet {
    val result = respond(header)
    return DoubleRatchet.responder(result.sharedSecret, signedPreKey.keyPair, result.associatedData, random)
}
