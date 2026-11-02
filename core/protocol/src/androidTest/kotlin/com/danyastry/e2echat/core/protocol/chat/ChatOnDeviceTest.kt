package com.danyastry.e2echat.core.protocol.chat

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.danyastry.e2echat.core.protocol.x3dh.X3dhDirectory
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatOnDeviceTest {

    @Test
    fun twoClientsTalkThroughTheRelayOnTheDevice() {
        val directory = X3dhDirectory()
        val relay = Relay()
        val user1 = ChatClient("User 1", directory, relay)
        val user2 = ChatClient("User 2", directory, relay)

        user1.send("User 2", "Hello")
        val first = relay.take("User 2").map(user2::receive)
        user2.send("User 1", "Hi")
        val reply = relay.take("User 1").map(user1::receive)

        assertThat(first).containsExactly("Hello")
        assertThat(reply).containsExactly("Hi")
    }
}
