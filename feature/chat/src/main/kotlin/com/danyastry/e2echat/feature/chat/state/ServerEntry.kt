package com.danyastry.e2echat.feature.chat.state

import com.danyastry.e2echat.feature.chat.model.EnvelopeKind
import com.danyastry.e2echat.feature.chat.model.Person

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
