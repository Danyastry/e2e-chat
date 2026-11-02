package com.danyastry.e2echat.feature.ratchet.state

import com.danyastry.e2echat.feature.ratchet.model.Participant

data class LogEntry(
    val sender: Participant,
    val ratchetKey: String,
    val previousChainLength: Int,
    val messageNumber: Int,
    val ciphertextBytes: Int,
    val receiverMadeDhStep: Boolean,
    val readText: String,
)
