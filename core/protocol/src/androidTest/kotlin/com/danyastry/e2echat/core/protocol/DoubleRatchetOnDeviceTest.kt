package com.danyastry.e2echat.core.protocol

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DoubleRatchetOnDeviceTest {

    @Test
    fun aConversationWorksOnTheDevice() {
        val server = KeyServer()
        val user2Keys = X3dhUser("User 2").also { server.publish("User 2", it.publishedKeys()) }
        val initiation = X3dhUser("User 1").initiate(server.fetchBundle("User 2"))
        val responder = user2Keys.respond(initiation.header)
        val user1 = DoubleRatchet.initiator(initiation.result.sharedSecret, user2Keys.signedPreKey.keyPair.publicKey, initiation.result.associatedData)
        val user2 = DoubleRatchet.responder(responder.sharedSecret, user2Keys.signedPreKey.keyPair, responder.associatedData)

        repeat(5) { round ->
            assertThat(user2.decrypt(user1.encrypt("ping $round".encodeToByteArray())).decodeToString()).isEqualTo("ping $round")
            assertThat(user1.decrypt(user2.encrypt("pong $round".encodeToByteArray())).decodeToString()).isEqualTo("pong $round")
        }
    }
}
