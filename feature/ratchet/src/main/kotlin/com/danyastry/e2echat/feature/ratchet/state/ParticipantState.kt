package com.danyastry.e2echat.feature.ratchet.state

data class ParticipantState(
    val rootKey: String,
    val ratchetKey: String,
    val sendingChain: String?,
    val sent: Int,
    val receivingChain: String?,
    val received: Int,
)
