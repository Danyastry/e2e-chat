package com.danyastry.e2echat.core.protocol.conversation

import com.danyastry.e2echat.core.protocol.chat.PreKeyEnvelope
import com.danyastry.e2echat.core.protocol.conversation.Participants.USER_1
import com.danyastry.e2echat.core.protocol.conversation.Participants.USER_2
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhDirectory
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class ConversationTest {

    private val conversation = Conversation.x3dh()

    @Test
    fun `messages wait on the relay until they are delivered`() {
        val first = conversation.send(USER_1, "one")
        val second = conversation.send(USER_1, "two")

        assertThat(conversation.isWaiting(first)).isTrue()
        assertThat(conversation.isWaiting(second)).isTrue()
        assertThat(conversation.deliverAll(USER_2)).containsExactly("one", "two").inOrder()
        assertThat(conversation.isWaiting(first)).isFalse()
        assertThat(conversation.log).containsExactly(first, second).inOrder()
    }

    @Test
    fun `send and deliver answers in both directions and only the first envelope carries the handshake`() {
        val hello = conversation.sendAndDeliver(USER_1, "Hello")
        val reply = conversation.sendAndDeliver(USER_2, "Hi")
        val next = conversation.sendAndDeliver(USER_1, "How are you")

        assertThat(hello).isInstanceOf(PreKeyEnvelope::class.java)
        assertThat(reply).isNotInstanceOf(PreKeyEnvelope::class.java)
        assertThat(next).isNotInstanceOf(PreKeyEnvelope::class.java)
        assertThat(conversation.log.none(conversation::isWaiting)).isTrue()
    }

    @Test
    fun `held envelopes can be delivered later and out of order`() {
        conversation.send(USER_1, "first")
        conversation.send(USER_1, "second")
        val held = conversation.takeWaiting(USER_2)

        assertThat(conversation.receive(USER_2, held[1])).isEqualTo("second")
        assertThat(conversation.receive(USER_2, held[0])).isEqualTo("first")
    }

    @Test
    fun `a stolen session is a copy that keeps working on its own`() {
        conversation.sendAndDeliver(USER_1, "Hello")
        val stolen = conversation.stealSession(USER_2)

        val envelope = conversation.send(USER_1, "Secret")

        assertThat(stolen.decrypt(envelope.message).decodeToString()).isEqualTo("Secret")
        assertThat(conversation.deliverAll(USER_2)).containsExactly("Secret")
    }

    @Test
    fun `the same conversation runs on the post-quantum handshake`() {
        val quantumSafe = Conversation(PqxdhDirectory())

        quantumSafe.sendAndDeliver(USER_1, "Hello after quantum")

        assertThat(quantumSafe.sendAndDeliver(USER_2, "Still private")).isNotNull()
        assertThat(quantumSafe.log).hasSize(2)
    }

    @Test
    fun `only the two participants can take part`() {
        assertThrows(IllegalArgumentException::class.java) { conversation.send("User 3", "Hi") }
        assertThat(Participants.other(USER_1)).isEqualTo(USER_2)
        assertThat(Participants.other(USER_2)).isEqualTo(USER_1)
    }
}
