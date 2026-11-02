package com.danyastry.e2echat.core.protocol.chat

import com.danyastry.e2echat.core.protocol.handshake.HandshakeHeader
import com.danyastry.e2echat.core.protocol.ratchet.MessageHeader
import com.danyastry.e2echat.core.protocol.ratchet.RatchetMessage

class PreKeyEnvelope(
    override val id: Int,
    override val from: String,
    override val to: String,
    val initial: HandshakeHeader,
    override val message: RatchetMessage,
) : Envelope {
    override val size: Int
        get() = initial.size + MessageHeader.SIZE + message.ciphertext.size
}
