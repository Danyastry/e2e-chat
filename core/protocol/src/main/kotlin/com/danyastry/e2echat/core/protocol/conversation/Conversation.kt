package com.danyastry.e2echat.core.protocol.conversation

import com.danyastry.e2echat.core.protocol.chat.ChatClient
import com.danyastry.e2echat.core.protocol.chat.Envelope
import com.danyastry.e2echat.core.protocol.chat.Relay
import com.danyastry.e2echat.core.protocol.conversation.Participants.USER_1
import com.danyastry.e2echat.core.protocol.conversation.Participants.USER_2
import com.danyastry.e2echat.core.protocol.handshake.HandshakeBundle
import com.danyastry.e2echat.core.protocol.handshake.HandshakeHeader
import com.danyastry.e2echat.core.protocol.handshake.KeyDirectory
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet
import com.danyastry.e2echat.core.protocol.x3dh.X3dhDirectory

class Conversation<Bundle : HandshakeBundle, Header : HandshakeHeader>(directory: KeyDirectory<Bundle, Header>) {

    private val relay = Relay()
    private val user1 = ChatClient(USER_1, directory, relay)
    private val user2 = ChatClient(USER_2, directory, relay)

    val log: List<Envelope>
        get() = relay.log

    fun isWaiting(envelope: Envelope): Boolean = relay.isWaiting(envelope)

    fun send(from: String, text: String): Envelope = client(from).send(Participants.other(from), text)

    fun sendAndDeliver(from: String, text: String): Envelope {
        val envelope = send(from, text)
        deliverAll(Participants.other(from))
        return envelope
    }

    fun deliverAll(to: String): List<String> = relay.take(to).map(client(to)::receive)

    fun receive(to: String, envelope: Envelope): String = client(to).receive(envelope)

    fun takeWaiting(to: String): List<Envelope> = relay.take(to)

    fun stealSession(of: String): DoubleRatchet =
        checkNotNull(client(of).session(Participants.other(of))) { "$of has no session yet" }.copy()

    private fun client(name: String) = when (name) {
        USER_1 -> user1
        USER_2 -> user2
        else -> throw IllegalArgumentException("$name is not in this conversation")
    }

    companion object {
        fun x3dh(): Conversation<*, *> = Conversation(X3dhDirectory())
    }
}
