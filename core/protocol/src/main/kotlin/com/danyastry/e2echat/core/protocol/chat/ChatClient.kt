package com.danyastry.e2echat.core.protocol.chat

import com.danyastry.e2echat.core.protocol.keys.KeyServer
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet
import com.danyastry.e2echat.core.protocol.x3dh.InitialHeader
import com.danyastry.e2echat.core.protocol.x3dh.X3dhUser
import java.security.SecureRandom

class ChatClient(
    val name: String,
    private val keyServer: KeyServer,
    private val relay: Relay,
    random: SecureRandom = SecureRandom(),
) {
    private val keys = X3dhUser(name, random = random)
    private val sessions = mutableMapOf<String, DoubleRatchet>()
    private val pendingInitial = mutableMapOf<String, InitialHeader>()
    private val acceptedHandshakes = mutableMapOf<String, ByteArray>()

    init {
        keyServer.publish(name, keys.publishedKeys())
    }

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
            val result = keys.respond(envelope.initial)
            sessions[envelope.from] = DoubleRatchet.responder(result.sharedSecret, keys.signedPreKey.keyPair, result.associatedData)
            acceptedHandshakes[envelope.from] = envelope.initial.ephemeralKey
        }
        val session = checkNotNull(sessions[envelope.from]) { "No session with ${envelope.from}" }
        val plaintext = session.decrypt(envelope.message).decodeToString()
        pendingInitial.remove(envelope.from)
        return plaintext
    }

    private fun startSession(to: String): DoubleRatchet {
        val bundle = keyServer.fetchBundle(to)
        val initiation = keys.initiate(bundle)
        pendingInitial[to] = initiation.header
        return DoubleRatchet.initiator(initiation.result.sharedSecret, bundle.signedPreKey, initiation.result.associatedData)
            .also { sessions[to] = it }
    }
}
