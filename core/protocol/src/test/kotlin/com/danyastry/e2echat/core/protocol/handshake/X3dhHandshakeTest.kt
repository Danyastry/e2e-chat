package com.danyastry.e2echat.core.protocol.handshake

import com.danyastry.e2echat.core.protocol.keys.PreKeyBundle
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhDirectory
import com.danyastry.e2echat.core.protocol.x3dh.InitialHeader
import com.danyastry.e2echat.core.protocol.x3dh.X3dhDirectory

class X3dhHandshakeTest : HandshakeContract<PreKeyBundle, InitialHeader>() {

    override fun directory(): KeyDirectory<PreKeyBundle, InitialHeader> = X3dhDirectory()

    override fun foreignHeader(): HandshakeHeader {
        val other = PqxdhDirectory()
        other.register("Other 2")
        return other.register("Other 1").initiate(other.fetchBundle("Other 2")).header
    }

    override val usesKem = false
}
