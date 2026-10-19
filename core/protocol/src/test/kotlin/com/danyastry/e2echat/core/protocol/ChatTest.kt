package com.danyastry.e2echat.core.protocol

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ChatTest {

    private val keyServer = KeyServer()
    private val relay = Relay()
    private val user1 = ChatClient("User 1", keyServer, relay)
    private val user2 = ChatClient("User 2", keyServer, relay)

    @Test
    fun `the first message carries the x3dh header and opens a session`() {
        val envelope = user1.send("User 2", "Hello")

        assertThat(envelope).isInstanceOf(PreKeyEnvelope::class.java)
        assertThat(deliver("User 2")).containsExactly("Hello")
        assertThat(user2.hasSessionWith("User 1")).isTrue()
    }

    @Test
    fun `messages wait on the server while the receiver is offline`() {
        user1.send("User 2", "one")
        user1.send("User 2", "two")

        assertThat(relay.waiting("User 2")).isEqualTo(2)
        assertThat(deliver("User 2")).containsExactly("one", "two").inOrder()
        assertThat(relay.waiting("User 2")).isEqualTo(0)
    }

    @Test
    fun `every message keeps the x3dh header until the other side replies`() {
        val first = user1.send("User 2", "one")
        val second = user1.send("User 2", "two")
        deliver("User 2")
        user2.send("User 1", "hi")
        deliver("User 1")

        val afterReply = user1.send("User 2", "three")

        assertThat(first).isInstanceOf(PreKeyEnvelope::class.java)
        assertThat(second).isInstanceOf(PreKeyEnvelope::class.java)
        assertThat(afterReply).isInstanceOf(MessageEnvelope::class.java)
        assertThat(deliver("User 2")).containsExactly("three")
    }

    @Test
    fun `a conversation works both ways`() {
        user1.send("User 2", "Hello")
        deliver("User 2")
        repeat(5) { round ->
            user2.send("User 1", "pong $round")
            assertThat(deliver("User 1")).containsExactly("pong $round")
            user1.send("User 2", "ping $round")
            assertThat(deliver("User 2")).containsExactly("ping $round")
        }
    }

    @Test
    fun `user 2 can start the chat too`() {
        user2.send("User 1", "Hi from User 2")

        assertThat(deliver("User 1")).containsExactly("Hi from User 2")
        user1.send("User 2", "Hi back")
        assertThat(deliver("User 2")).containsExactly("Hi back")
    }

    @Test
    fun `the server log holds only ciphertext`() {
        user1.send("User 2", "Meet me at the old bridge")

        val envelope = relay.log.single()
        assertThat(envelope.message.ciphertext.decodeToString()).doesNotContain("bridge")
        assertThat(envelope.from).isEqualTo("User 1")
        assertThat(envelope.to).isEqualTo("User 2")
    }

    @Test
    fun `a prekey message is bigger because of the x3dh header`() {
        val first = user1.send("User 2", "Hello")
        deliver("User 2")
        user2.send("User 1", "Hi")
        deliver("User 1")
        val normal = user1.send("User 2", "Hello")

        assertThat(first.size - normal.size).isEqualTo(72)
    }

    @Test
    fun `an envelope for someone else is refused`() {
        val envelope = user1.send("User 2", "Hello")

        assertThrows(IllegalArgumentException::class.java) { user1.receive(envelope) }
    }

    private fun deliver(to: String): List<String> {
        val client = if (to == "User 1") user1 else user2
        return relay.take(to).map(client::receive)
    }
}
