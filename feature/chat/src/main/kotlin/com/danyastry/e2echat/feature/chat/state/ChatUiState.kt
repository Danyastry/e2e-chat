package com.danyastry.e2echat.feature.chat.state

data class ChatUiState(
    val user1: List<Bubble> = emptyList(),
    val user2: List<Bubble> = emptyList(),
    val user1Input: String = "",
    val user2Input: String = "",
    val isUser2Online: Boolean = true,
    val server: List<ServerEntry> = emptyList(),
) {
    val waitingForUser2: Int
        get() = server.count { it.isWaiting }
}
