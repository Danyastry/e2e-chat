package com.danyastry.e2echat.core.protocol

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatOnDeviceTest {

    @Test
    fun twoClientsTalkThroughTheRelayOnTheDevice() {
        val keyServer = KeyServer()
        val relay = Relay()
        val user1 = ChatClient("User 1", keyServer, relay)
        val user2 = ChatClient("User 2", keyServer, relay)

        user1.send("User 2", "Hello")
        val first = relay.take("User 2").map(user2::receive)
        user2.send("User 1", "Hi")
        val reply = relay.take("User 1").map(user1::receive)

        assertThat(first).containsExactly("Hello")
        assertThat(reply).containsExactly("Hi")
    }
}
