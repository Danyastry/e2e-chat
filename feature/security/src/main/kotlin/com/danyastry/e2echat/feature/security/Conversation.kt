package com.danyastry.e2echat.feature.security

import com.danyastry.e2echat.core.protocol.ChatClient
import com.danyastry.e2echat.core.protocol.DoubleRatchet
import com.danyastry.e2echat.core.protocol.Envelope
import com.danyastry.e2echat.core.protocol.KeyServer
import com.danyastry.e2echat.core.protocol.Relay

class Conversation {

    private val keyServer = KeyServer()
    private val relay = Relay()
    private val user1 = ChatClient(USER_1, keyServer, relay)
    private val user2 = ChatClient(USER_2, keyServer, relay)

    fun send(from: String, text: String): Envelope = client(from).send(other(from), text)

    fun sendAndDeliver(from: String, text: String): Envelope {
        val envelope = send(from, text)
        deliverAll(other(from))
        return envelope
    }

    fun deliverAll(to: String): List<String> = relay.take(to).map(client(to)::receive)

    fun receive(to: String, envelope: Envelope): String = client(to).receive(envelope)

    fun takeWaiting(to: String): List<Envelope> = relay.take(to)

    fun stealSession(of: String): DoubleRatchet = checkNotNull(client(of).session(other(of))) { "$of has no session yet" }.copy()

    private fun client(name: String) = if (name == USER_1) user1 else user2

    private fun other(name: String) = if (name == USER_1) USER_2 else USER_1

    companion object {
        const val USER_1 = "User 1"
        const val USER_2 = "User 2"
    }
}

fun DoubleRatchet.tryRead(envelope: Envelope): String? = runCatching { decrypt(envelope.message).decodeToString() }.getOrNull()
