package com.danyastry.e2echat.feature.chat.ui

import androidx.lifecycle.ViewModel
import com.danyastry.e2echat.core.crypto.Fingerprint
import com.danyastry.e2echat.core.protocol.ChatClient
import com.danyastry.e2echat.core.protocol.Envelope
import com.danyastry.e2echat.core.protocol.KeyServer
import com.danyastry.e2echat.core.protocol.PreKeyEnvelope
import com.danyastry.e2echat.core.protocol.Relay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChatViewModel : ViewModel() {

    private val keyServer = KeyServer()
    private val relay = Relay()
    private val clients = mapOf(
        Person.USER_1 to ChatClient(Person.USER_1.label, keyServer, relay),
        Person.USER_2 to ChatClient(Person.USER_2.label, keyServer, relay),
    )
    private val delivered = mutableSetOf<Int>()

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    fun onInputChange(person: Person, text: String) {
        _state.update { if (person == Person.USER_1) it.copy(user1Input = text) else it.copy(user2Input = text) }
    }

    fun onSend(person: Person) {
        val snapshot = _state.value
        val text = if (person == Person.USER_1) snapshot.user1Input else snapshot.user2Input
        if (text.isBlank() || (person == Person.USER_2 && !snapshot.isUser2Online)) return
        clients.getValue(person).send(person.other.label, text)
        _state.update { state ->
            val bubble = Bubble(text, isMine = true)
            if (person == Person.USER_1) state.copy(user1 = state.user1 + bubble, user1Input = "") else state.copy(user2 = state.user2 + bubble, user2Input = "")
        }
        deliverIfOnline(person.other)
    }

    fun onOnlineChange(isOnline: Boolean) {
        _state.update { it.copy(isUser2Online = isOnline) }
        deliverIfOnline(Person.USER_2)
    }

    private fun deliverIfOnline(person: Person) {
        if (person == Person.USER_2 && !_state.value.isUser2Online) {
            refreshServer()
            return
        }
        val received = relay.take(person.label).map { envelope ->
            delivered += envelope.id
            Bubble(clients.getValue(person).receive(envelope), isMine = false)
        }
        _state.update { state ->
            if (person == Person.USER_1) state.copy(user1 = state.user1 + received) else state.copy(user2 = state.user2 + received)
        }
        refreshServer()
    }

    private fun refreshServer() {
        _state.update { it.copy(server = relay.log.reversed().map(::toEntry)) }
    }

    private fun toEntry(envelope: Envelope) = ServerEntry(
        id = envelope.id,
        from = if (envelope.from == Person.USER_1.label) Person.USER_1 else Person.USER_2,
        kind = if (envelope is PreKeyEnvelope) EnvelopeKind.PRE_KEY else EnvelopeKind.MESSAGE,
        ratchetKey = Fingerprint.of(envelope.message.header.ratchetKey),
        previousChainLength = envelope.message.header.previousChainLength,
        messageNumber = envelope.message.header.messageNumber,
        bytes = envelope.size,
        preview = envelope.message.ciphertext.take(PREVIEW_BYTES).joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') },
        isWaiting = envelope.id !in delivered,
    )

    private val Person.label: String
        get() = if (this == Person.USER_1) "User 1" else "User 2"

    private val Person.other: Person
        get() = if (this == Person.USER_1) Person.USER_2 else Person.USER_1

    private companion object {
        const val PREVIEW_BYTES = 12
    }
}
