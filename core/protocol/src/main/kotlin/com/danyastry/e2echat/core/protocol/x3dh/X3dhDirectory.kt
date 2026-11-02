package com.danyastry.e2echat.core.protocol.x3dh

import com.danyastry.e2echat.core.protocol.handshake.Handshake
import com.danyastry.e2echat.core.protocol.handshake.KeyDirectory
import com.danyastry.e2echat.core.protocol.keys.KeyServer
import com.danyastry.e2echat.core.protocol.keys.PreKeyBundle
import java.security.SecureRandom

class X3dhDirectory(val server: KeyServer = KeyServer()) : KeyDirectory<PreKeyBundle, InitialHeader> {

    override fun register(user: String, random: SecureRandom): Handshake<PreKeyBundle, InitialHeader> =
        X3dhUser(user, random = random).also { server.publish(user, it.publishedKeys()) }

    override fun fetchBundle(user: String): PreKeyBundle = server.fetchBundle(user)
}
