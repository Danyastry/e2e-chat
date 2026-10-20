package com.danyastry.e2echat.feature.pqxdh

import com.danyastry.e2echat.core.crypto.KeyPair
import com.danyastry.e2echat.core.crypto.MlKem
import com.danyastry.e2echat.core.protocol.DoubleRatchet
import com.danyastry.e2echat.core.protocol.KeyServer
import com.danyastry.e2echat.core.protocol.Pqxdh
import com.danyastry.e2echat.core.protocol.PqxdhKeyServer
import com.danyastry.e2echat.core.protocol.PqxdhUser
import com.danyastry.e2echat.core.protocol.RatchetMessage
import com.danyastry.e2echat.core.protocol.X3dh
import com.danyastry.e2echat.core.protocol.X3dhUser

enum class Handshake { X3DH, PQXDH }

data class Recording(val handshake: Handshake, val bundleBytes: Int, val headerBytes: Int, val messages: List<String>)

data class QuantumAttack(val handshake: Handshake, val dhOutputsBroken: Int, val attackerRead: List<String?>) {
    val isReadable: Boolean
        get() = attackerRead.all { it != null }
}

private class RecordedSession(
    val recording: Recording,
    val ciphertexts: List<RatchetMessage>,
    val dhOutputs: List<ByteArray>,
    val associatedData: ByteArray,
    val responderRatchetKey: KeyPair,
)

class HarvestDemo {

    private var sessions: List<RecordedSession> = emptyList()

    val isRecorded: Boolean
        get() = sessions.isNotEmpty()

    fun record(): List<Recording> {
        sessions = listOf(recordX3dh(), recordPqxdh())
        return sessions.map { it.recording }
    }

    fun attack(): List<QuantumAttack> = sessions.map { session ->
        val guessedSecret = when (session.recording.handshake) {
            Handshake.X3DH -> X3dh.deriveSecret(session.dhOutputs)
            Handshake.PQXDH -> Pqxdh.deriveSecret(session.dhOutputs, ByteArray(MlKem.SHARED_SECRET_BYTES))
        }
        val attacker = DoubleRatchet.responder(guessedSecret, session.responderRatchetKey, session.associatedData)
        val read = session.ciphertexts.map { message -> runCatching { attacker.decrypt(message).decodeToString() }.getOrNull() }
        QuantumAttack(session.recording.handshake, session.dhOutputs.size, read)
    }

    private fun recordX3dh(): RecordedSession {
        val server = KeyServer()
        val user1 = X3dhUser(USER_1)
        val user2 = X3dhUser(USER_2).also { server.publish(USER_2, it.publishedKeys()) }
        val bundle = server.fetchBundle(USER_2)
        val initiation = user1.initiate(bundle)
        user2.respond(initiation.header)
        val sender = DoubleRatchet.initiator(initiation.result.sharedSecret, bundle.signedPreKey, initiation.result.associatedData)
        return RecordedSession(
            recording = Recording(Handshake.X3DH, bundle.size, initiation.header.size, MESSAGES),
            ciphertexts = MESSAGES.map { sender.encrypt(it.encodeToByteArray()) },
            dhOutputs = initiation.result.dhOutputs,
            associatedData = initiation.result.associatedData,
            responderRatchetKey = user2.signedPreKey.keyPair,
        )
    }

    private fun recordPqxdh(): RecordedSession {
        val server = PqxdhKeyServer()
        val user1 = PqxdhUser(USER_1)
        val user2 = PqxdhUser(USER_2).also { server.publish(USER_2, it.publishedKeys()) }
        val bundle = server.fetchBundle(USER_2)
        val initiation = user1.initiate(bundle)
        user2.respond(initiation.header)
        val sender = DoubleRatchet.initiator(initiation.result.sharedSecret, bundle.classical.signedPreKey, initiation.result.associatedData)
        return RecordedSession(
            recording = Recording(Handshake.PQXDH, bundle.size, initiation.header.size, MESSAGES),
            ciphertexts = MESSAGES.map { sender.encrypt(it.encodeToByteArray()) },
            dhOutputs = initiation.result.dhOutputs,
            associatedData = initiation.result.associatedData,
            responderRatchetKey = user2.signedPreKey.keyPair,
        )
    }

    companion object {
        const val USER_1 = "User 1"
        const val USER_2 = "User 2"
        val MESSAGES = listOf("The launch is on Friday", "Keep it quiet until then")
    }
}

data class NewChat(val number: Int, val kemPreKeyId: Int, val isLastResort: Boolean, val oneTimeKemPreKeysLeft: Int, val agreed: Boolean)

class PreKeyDemo(private val oneTimeKemPreKeys: Int = PqxdhUser.DEFAULT_ONE_TIME_KEM_PREKEYS) {

    private val server = PqxdhKeyServer()
    private val user2 = PqxdhUser(HarvestDemo.USER_2, oneTimeKemPreKeyCount = oneTimeKemPreKeys)
        .also { server.publish(HarvestDemo.USER_2, it.publishedKeys()) }
    private var count = 0

    val oneTimeKemPreKeysLeft: Int
        get() = server.oneTimeKemPreKeysLeft(HarvestDemo.USER_2)

    fun startChat(): NewChat {
        val initiator = PqxdhUser("Contact ${++count}", oneTimePreKeyCount = 0, oneTimeKemPreKeyCount = 0)
        val bundle = server.fetchBundle(HarvestDemo.USER_2)
        val initiation = initiator.initiate(bundle)
        val agreed = user2.respond(initiation.header).sharedSecret.contentEquals(initiation.result.sharedSecret)
        return NewChat(count, bundle.kemPreKey.id, bundle.isLastResort, oneTimeKemPreKeysLeft, agreed)
    }
}
