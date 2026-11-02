package com.danyastry.e2echat.feature.ratchet.ui

import androidx.lifecycle.ViewModel
import com.danyastry.e2echat.core.crypto.fingerprint.Fingerprint
import com.danyastry.e2echat.core.protocol.keys.KeyServer
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet
import com.danyastry.e2echat.core.protocol.ratchet.RatchetSnapshot
import com.danyastry.e2echat.core.protocol.session.acceptSession
import com.danyastry.e2echat.core.protocol.session.startSession
import com.danyastry.e2echat.core.protocol.x3dh.X3dhUser
import com.danyastry.e2echat.feature.ratchet.model.Participant
import com.danyastry.e2echat.feature.ratchet.state.LogEntry
import com.danyastry.e2echat.feature.ratchet.state.ParticipantState
import com.danyastry.e2echat.feature.ratchet.state.RatchetUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RatchetViewModel : ViewModel() {

    private val _state = MutableStateFlow(RatchetUiState())
    val state: StateFlow<RatchetUiState> = _state.asStateFlow()

    private lateinit var sessions: Map<Participant, DoubleRatchet>

    init {
        onRestart()
    }

    fun onRestart() {
        val server = KeyServer()
        val user2 = X3dhUser("User 2").also { server.publish("User 2", it.publishedKeys()) }
        val start = X3dhUser("User 1").startSession(server.fetchBundle("User 2"))
        sessions = mapOf(
            Participant.USER_1 to start.session,
            Participant.USER_2 to user2.acceptSession(start.header),
        )
        _state.update { RatchetUiState(input = it.input).withSnapshots() }
    }

    fun onInputChange(input: String) {
        _state.update { it.copy(input = input) }
    }

    fun onSend(sender: Participant) {
        val text = _state.value.input
        if (text.isEmpty()) return
        if (sender == Participant.USER_2 && !_state.value.canUser2Send) return
        val receiver = if (sender == Participant.USER_1) Participant.USER_2 else Participant.USER_1
        val message = sessions.getValue(sender).encrypt(text.encodeToByteArray())
        val rootBefore = sessions.getValue(receiver).snapshot().rootKey
        val read = sessions.getValue(receiver).decrypt(message).decodeToString()
        val entry = LogEntry(
            sender = sender,
            ratchetKey = Fingerprint.of(message.header.ratchetKey),
            previousChainLength = message.header.previousChainLength,
            messageNumber = message.header.messageNumber,
            ciphertextBytes = message.ciphertext.size,
            receiverMadeDhStep = !sessions.getValue(receiver).snapshot().rootKey.contentEquals(rootBefore),
            readText = read,
        )
        _state.update { it.copy(log = listOf(entry) + it.log).withSnapshots() }
    }

    private fun RatchetUiState.withSnapshots() = copy(
        user1 = sessions.getValue(Participant.USER_1).snapshot().toState(),
        user2 = sessions.getValue(Participant.USER_2).snapshot().toState(),
    )

    private fun RatchetSnapshot.toState() = ParticipantState(
        rootKey = Fingerprint.of(rootKey),
        ratchetKey = Fingerprint.of(ownRatchetKey),
        sendingChain = sendingChainKey?.let(Fingerprint::of),
        sent = sent,
        receivingChain = receivingChainKey?.let(Fingerprint::of),
        received = received,
    )
}
