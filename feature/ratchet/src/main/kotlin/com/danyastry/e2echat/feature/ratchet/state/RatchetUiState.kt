package com.danyastry.e2echat.feature.ratchet.state

data class RatchetUiState(
    val user1: ParticipantState? = null,
    val user2: ParticipantState? = null,
    val input: String = "Hello",
    val log: List<LogEntry> = emptyList(),
) {
    val canUser2Send: Boolean
        get() = user2?.sendingChain != null
}
