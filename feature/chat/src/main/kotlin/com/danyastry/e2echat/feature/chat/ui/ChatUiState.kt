package com.danyastry.e2echat.feature.chat.ui

enum class Person { USER_1, USER_2 }

data class Bubble(val text: String, val isMine: Boolean)

enum class EnvelopeKind { PRE_KEY, MESSAGE }

data class ServerEntry(
    val id: Int,
    val from: Person,
    val kind: EnvelopeKind,
    val ratchetKey: String,
    val previousChainLength: Int,
    val messageNumber: Int,
    val bytes: Int,
    val preview: String,
    val isWaiting: Boolean,
)

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
