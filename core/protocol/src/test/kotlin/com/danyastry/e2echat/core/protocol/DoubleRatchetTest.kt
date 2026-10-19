package com.danyastry.e2echat.core.protocol

import com.danyastry.e2echat.core.crypto.AuthenticationException
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class DoubleRatchetTest {

    private val server = KeyServer()
    private val user2Keys = X3dhUser("User 2").also { server.publish("User 2", it.publishedKeys()) }
    private val initiation = X3dhUser("User 1").initiate(server.fetchBundle("User 2"))
    private val responder = user2Keys.respond(initiation.header)

    private val user1 = DoubleRatchet.initiator(initiation.result.sharedSecret, user2Keys.signedPreKey.keyPair.publicKey, initiation.result.associatedData)
    private val user2 = DoubleRatchet.responder(responder.sharedSecret, user2Keys.signedPreKey.keyPair, responder.associatedData)

    @Test
    fun `user 2 reads the first message right after x3dh`() {
        assertThat(user2.decrypt(user1.encrypt(text("Hello"))).decodeToString()).isEqualTo("Hello")
    }

    @Test
    fun `a long conversation works in both directions`() {
        repeat(20) { round ->
            assertThat(user2.decrypt(user1.encrypt(text("ping $round"))).decodeToString()).isEqualTo("ping $round")
            assertThat(user1.decrypt(user2.encrypt(text("pong $round"))).decodeToString()).isEqualTo("pong $round")
        }
    }

    @Test
    fun `several messages in a row each get their own key`() {
        val messages = List(3) { user1.encrypt(text("same text")) }

        assertThat(messages.map { it.header.messageNumber }).containsExactly(0, 1, 2).inOrder()
        assertThat(messages.map { it.ciphertext.toList() }.toSet()).hasSize(3)
        messages.forEach { assertThat(user2.decrypt(it).decodeToString()).isEqualTo("same text") }
    }

    @Test
    fun `a reply moves both sides to a new ratchet key`() {
        user2.decrypt(user1.encrypt(text("Hello")))
        val before = user1.snapshot().ownRatchetKey

        val reply = user2.encrypt(text("Hi"))
        user1.decrypt(reply)
        val after = user1.encrypt(text("How are you?"))

        assertThat(reply.header.ratchetKey).isNotEqualTo(user2Keys.signedPreKey.keyPair.publicKey)
        assertThat(after.header.ratchetKey).isNotEqualTo(before)
        assertThat(after.header.messageNumber).isEqualTo(0)
        assertThat(after.header.previousChainLength).isEqualTo(1)
    }

    @Test
    fun `messages can arrive out of order within a chain`() {
        val first = user1.encrypt(text("one"))
        val second = user1.encrypt(text("two"))
        val third = user1.encrypt(text("three"))

        assertThat(user2.decrypt(third).decodeToString()).isEqualTo("three")
        assertThat(user2.snapshot().skippedKeys).isEqualTo(2)
        assertThat(user2.decrypt(first).decodeToString()).isEqualTo("one")
        assertThat(user2.decrypt(second).decodeToString()).isEqualTo("two")
        assertThat(user2.snapshot().skippedKeys).isEqualTo(0)
    }

    @Test
    fun `a late message from an old chain still opens after a ratchet step`() {
        val early = user1.encrypt(text("early"))
        val late = user1.encrypt(text("late"))
        user2.decrypt(early)
        user1.decrypt(user2.encrypt(text("reply")))
        val newChain = user1.encrypt(text("new chain"))

        assertThat(user2.decrypt(newChain).decodeToString()).isEqualTo("new chain")
        assertThat(user2.decrypt(late).decodeToString()).isEqualTo("late")
    }

    @Test
    fun `a replayed message is rejected`() {
        val message = user1.encrypt(text("Pay 100 dollars"))
        user2.decrypt(message)

        assertThrows(AuthenticationException::class.java) { user2.decrypt(message) }
    }

    @Test
    fun `a changed ciphertext is rejected and the session keeps working`() {
        val message = user1.encrypt(text("Hello"))
        val tampered = RatchetMessage(message.header, message.ciphertext.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() })

        assertThrows(AuthenticationException::class.java) { user2.decrypt(tampered) }
        assertThat(user2.decrypt(message).decodeToString()).isEqualTo("Hello")
    }

    @Test
    fun `a changed header is rejected because it is authenticated`() {
        val message = user1.encrypt(text("Hello"))
        val header = message.header
        val tampered = RatchetMessage(MessageHeader(header.ratchetKey, header.previousChainLength + 1, header.messageNumber), message.ciphertext)

        assertThrows(AuthenticationException::class.java) { user2.decrypt(tampered) }
        assertThat(user2.decrypt(message).decodeToString()).isEqualTo("Hello")
    }

    @Test
    fun `a failed decryption does not change the session`() {
        val message = user1.encrypt(text("Hello"))
        val before = user2.snapshot()

        runCatching { user2.decrypt(RatchetMessage(message.header, ByteArray(48))) }

        val after = user2.snapshot()
        assertThat(after.received).isEqualTo(before.received)
        assertThat(after.skippedKeys).isEqualTo(before.skippedKeys)
        assertThat(after.rootKey).isEqualTo(before.rootKey)
    }

    @Test
    fun `refuses to skip more than the limit`() {
        repeat(DoubleRatchet.MAX_SKIP + 1) { user1.encrypt(text("lost")) }
        val tooFar = user1.encrypt(text("too far"))

        assertThrows(TooManySkippedMessagesException::class.java) { user2.decrypt(tooFar) }
    }

    @Test
    fun `user 2 cannot send before hearing from user 1`() {
        assertThrows(IllegalStateException::class.java) { user2.encrypt(text("Hi")) }
    }

    @Test
    fun `another session cannot read the messages`() {
        val other = X3dhUser("User 3").initiate(server.fetchBundle("User 2"))
        val stranger = DoubleRatchet.responder(other.result.sharedSecret, user2Keys.signedPreKey.keyPair, other.result.associatedData)

        assertThrows(AuthenticationException::class.java) { stranger.decrypt(user1.encrypt(text("Hello"))) }
    }

    @Test
    fun `the header is 40 bytes`() {
        assertThat(user1.encrypt(text("Hello")).header.encode()).hasLength(40)
    }

    @Test
    fun `a dh step moves the sent count into the previous chain length`() {
        user2.decrypt(user1.encrypt(text("a")))
        user2.decrypt(user1.encrypt(text("b")))
        user1.decrypt(user2.encrypt(text("c")))

        val one = user1.snapshot()
        val two = user2.snapshot()
        assertThat(two.received).isEqualTo(2)
        assertThat(two.sent).isEqualTo(1)
        assertThat(two.previousChainLength).isEqualTo(0)
        assertThat(one.received).isEqualTo(1)
        assertThat(one.sent).isEqualTo(0)
        assertThat(one.previousChainLength).isEqualTo(2)
    }

    @Test
    fun `a copy works on its own without touching the original`() {
        val first = user1.encrypt(text("one"))
        val second = user1.encrypt(text("two"))
        val copy = user2.copy()

        assertThat(copy.decrypt(first).decodeToString()).isEqualTo("one")
        assertThat(user2.snapshot().received).isEqualTo(0)
        assertThat(user2.decrypt(second).decodeToString()).isEqualTo("two")
        assertThat(copy.decrypt(second).decodeToString()).isEqualTo("two")
    }

    private fun text(value: String) = value.encodeToByteArray()
}
