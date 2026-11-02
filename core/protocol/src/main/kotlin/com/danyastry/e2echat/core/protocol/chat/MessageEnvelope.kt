package com.danyastry.e2echat.core.protocol.chat

import com.danyastry.e2echat.core.protocol.ratchet.RatchetMessage

class MessageEnvelope(
    override val id: Int,
    override val from: String,
    override val to: String,
    override val message: RatchetMessage,
) : Envelope
