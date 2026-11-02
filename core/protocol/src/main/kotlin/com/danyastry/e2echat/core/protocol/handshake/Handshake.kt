package com.danyastry.e2echat.core.protocol.handshake

import com.danyastry.e2echat.core.protocol.keys.SignedPreKey

interface Handshake<Bundle : HandshakeBundle, Header : HandshakeHeader> {
    val name: String

    val signedPreKey: SignedPreKey

    fun initiate(bundle: Bundle): HandshakeStart<Header>

    fun respond(header: Header): HandshakeResult

    fun headerOf(header: HandshakeHeader): Header?
}
