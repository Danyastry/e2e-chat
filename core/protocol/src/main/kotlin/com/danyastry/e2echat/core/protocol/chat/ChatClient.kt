package com.danyastry.e2echat.core.protocol.chat

import com.danyastry.e2echat.core.protocol.handshake.HandshakeBundle
import com.danyastry.e2echat.core.protocol.handshake.HandshakeHeader
import com.danyastry.e2echat.core.protocol.handshake.KeyDirectory
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet
import com.danyastry.e2echat.core.protocol.session.acceptSession
import com.danyastry.e2echat.core.protocol.session.startSession
import java.security.SecureRandom

class ChatClient<Bundle : HandshakeBundle, Header : HandshakeHeader>(
    val name: String,
    private val directory: KeyDirectory<Bundle, Header>,
    private val relay: Relay,
    private val random: SecureRandom = SecureRandom(),
) {
    private val handshake = directory.register(name, random)
    private val sessions = mutableMapOf<String, DoubleRatchet>()
    private val pendingInitial = mutableMapOf<String, Header>()
    private val acceptedHandshakes = mutableMapOf<String, ByteArray>()

    fun hasSessionWith(peer: String): Boolean = sessions.containsKey(peer)

    fun session(peer: String): DoubleRatchet? = sessions[peer]

    fun send(to: String, text: String): Envelope {
        val session = sessions[to] ?: startSession(to)
        val message = session.encrypt(text.encodeToByteArray())
        val initial = pendingInitial[to]
        val envelope = if (initial != null) PreKeyEnvelope(relay.newId(), name, to, initial, message) else MessageEnvelope(relay.newId(), name, to, message)
        relay.post(envelope)
        return envelope
    }

    fun receive(envelope: Envelope): String {
        require(envelope.to == name) { "Envelope for ${envelope.to} delivered to $name" }
        if (envelope is PreKeyEnvelope && !acceptedHandshakes[envelope.from].contentEquals(envelope.initial.ephemeralKey)) {
            val header = requireNotNull(handshake.headerOf(envelope.initial)) { "${envelope.from} started a handshake that $name does not speak" }
            sessions[envelope.from] = handshake.acceptSession(header, random)
            acceptedHandshakes[envelope.from] = envelope.initial.ephemeralKey
        }
        val session = checkNotNull(sessions[envelope.from]) { "No session with ${envelope.from}" }
        val plaintext = session.decrypt(envelope.message).decodeToString()
        pendingInitial.remove(envelope.from)
        return plaintext
    }

    private fun startSession(to: String): DoubleRatchet {
        val start = handshake.startSession(directory.fetchBundle(to), random)
        pendingInitial[to] = start.header
        return start.session.also { sessions[to] = it }
    }
}
