package com.danyastry.e2echat.core.protocol.chat

import com.danyastry.e2echat.core.protocol.ratchet.MessageHeader
import com.danyastry.e2echat.core.protocol.ratchet.RatchetMessage

sealed interface Envelope {
    val id: Int
    val from: String
    val to: String
    val message: RatchetMessage

    val size: Int
        get() = MessageHeader.SIZE + message.ciphertext.size
}
