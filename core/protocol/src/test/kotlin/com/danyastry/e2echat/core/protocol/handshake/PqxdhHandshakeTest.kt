package com.danyastry.e2echat.core.protocol.handshake

import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhBundle
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhDirectory
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhHeader
import com.danyastry.e2echat.core.protocol.x3dh.X3dhDirectory

class PqxdhHandshakeTest : HandshakeContract<PqxdhBundle, PqxdhHeader>() {

    override fun directory(): KeyDirectory<PqxdhBundle, PqxdhHeader> = PqxdhDirectory()

    override fun foreignHeader(): HandshakeHeader {
        val other = X3dhDirectory()
        other.register("Other 2")
        return other.register("Other 1").initiate(other.fetchBundle("Other 2")).header
    }

    override val usesKem = true
}
