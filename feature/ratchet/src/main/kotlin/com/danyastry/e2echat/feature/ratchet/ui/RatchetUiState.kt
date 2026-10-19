package com.danyastry.e2echat.feature.ratchet.ui

enum class Participant { USER_1, USER_2 }

data class ParticipantState(
    val rootKey: String,
    val ratchetKey: String,
    val sendingChain: String?,
    val sent: Int,
    val receivingChain: String?,
    val received: Int,
)

data class LogEntry(
    val sender: Participant,
    val ratchetKey: String,
    val previousChainLength: Int,
    val messageNumber: Int,
    val ciphertextBytes: Int,
    val receiverMadeDhStep: Boolean,
    val readText: String,
)

data class RatchetUiState(
    val user1: ParticipantState? = null,
    val user2: ParticipantState? = null,
    val input: String = "Hello",
    val log: List<LogEntry> = emptyList(),
) {
    val canUser2Send: Boolean
        get() = user2?.sendingChain != null
}
