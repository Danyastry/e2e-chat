package com.danyastry.e2echat.feature.pqxdh.logic

import com.danyastry.e2echat.core.crypto.kem.MlKem
import com.danyastry.e2echat.core.protocol.keys.KeyServer
import com.danyastry.e2echat.core.protocol.pqxdh.Pqxdh
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhKeyServer
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhUser
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet
import com.danyastry.e2echat.core.protocol.session.acceptSession
import com.danyastry.e2echat.core.protocol.session.startSession
import com.danyastry.e2echat.core.protocol.x3dh.X3dh
import com.danyastry.e2echat.core.protocol.x3dh.X3dhUser
import com.danyastry.e2echat.feature.pqxdh.model.Handshake
import com.danyastry.e2echat.feature.pqxdh.model.QuantumAttack
import com.danyastry.e2echat.feature.pqxdh.model.Recording

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
        val attacker = DoubleRatchet.responderFromRecoveredSecret(guessedSecret, session.responderRatchetKey, session.associatedData)
        val read = session.ciphertexts.map { message -> runCatching { attacker.decrypt(message).decodeToString() }.getOrNull() }
        QuantumAttack(session.recording.handshake, session.dhOutputs.size, read)
    }

    private fun recordX3dh(): RecordedSession {
        val server = KeyServer()
        val user1 = X3dhUser(USER_1)
        val user2 = X3dhUser(USER_2).also { server.publish(USER_2, it.publishedKeys()) }
        val bundle = server.fetchBundle(USER_2)
        val start = user1.startSession(bundle)
        user2.acceptSession(start.header)
        return RecordedSession(
            recording = Recording(Handshake.X3DH, bundle.size, start.header.size, MESSAGES),
            ciphertexts = MESSAGES.map { start.session.encrypt(it.encodeToByteArray()) },
            dhOutputs = start.handshake.dhOutputs,
            associatedData = start.handshake.associatedData,
            responderRatchetKey = user2.signedPreKey.keyPair,
        )
    }

    private fun recordPqxdh(): RecordedSession {
        val server = PqxdhKeyServer()
        val user1 = PqxdhUser(USER_1)
        val user2 = PqxdhUser(USER_2).also { server.publish(USER_2, it.publishedKeys()) }
        val bundle = server.fetchBundle(USER_2)
        val start = user1.startSession(bundle)
        user2.acceptSession(start.header)
        return RecordedSession(
            recording = Recording(Handshake.PQXDH, bundle.size, start.header.size, MESSAGES),
            ciphertexts = MESSAGES.map { start.session.encrypt(it.encodeToByteArray()) },
            dhOutputs = start.handshake.dhOutputs,
            associatedData = start.handshake.associatedData,
            responderRatchetKey = user2.signedPreKey.keyPair,
        )
    }

    companion object {
        const val USER_1 = "User 1"
        const val USER_2 = "User 2"
        val MESSAGES = listOf("The launch is on Friday", "Keep it quiet until then")
    }
}
