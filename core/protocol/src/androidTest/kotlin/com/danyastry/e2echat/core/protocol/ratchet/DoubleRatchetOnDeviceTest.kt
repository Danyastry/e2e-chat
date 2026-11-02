package com.danyastry.e2echat.core.protocol.ratchet

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.danyastry.e2echat.core.protocol.keys.KeyServer
import com.danyastry.e2echat.core.protocol.session.acceptSession
import com.danyastry.e2echat.core.protocol.session.startSession
import com.danyastry.e2echat.core.protocol.x3dh.X3dhUser
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DoubleRatchetOnDeviceTest {

    @Test
    fun aConversationWorksOnTheDevice() {
        val server = KeyServer()
        val user2Keys = X3dhUser("User 2").also { server.publish("User 2", it.publishedKeys()) }
        val start = X3dhUser("User 1").startSession(server.fetchBundle("User 2"))
        val user1 = start.session
        val user2 = user2Keys.acceptSession(start.header)

        repeat(5) { round ->
            assertThat(user2.decrypt(user1.encrypt("ping $round".encodeToByteArray())).decodeToString()).isEqualTo("ping $round")
            assertThat(user1.decrypt(user2.encrypt("pong $round".encodeToByteArray())).decodeToString()).isEqualTo("pong $round")
        }
    }
}
