package com.danyastry.e2echat.core.protocol.pqxdh

import com.danyastry.e2echat.core.protocol.handshake.Handshake
import com.danyastry.e2echat.core.protocol.handshake.KeyDirectory
import java.security.SecureRandom

class PqxdhDirectory(val server: PqxdhKeyServer = PqxdhKeyServer()) : KeyDirectory<PqxdhBundle, PqxdhHeader> {

    override fun register(user: String, random: SecureRandom): Handshake<PqxdhBundle, PqxdhHeader> =
        PqxdhUser(user, random = random).also { server.publish(user, it.publishedKeys()) }

    override fun fetchBundle(user: String): PqxdhBundle = server.fetchBundle(user)
}
