package com.danyastry.e2echat.core.protocol.ratchet

import com.danyastry.e2echat.core.crypto.curve.X25519
import java.nio.ByteBuffer

class MessageHeader(val ratchetKey: ByteArray, val previousChainLength: Int, val messageNumber: Int) {

    fun encode(): ByteArray = ByteBuffer.allocate(SIZE).put(ratchetKey).putInt(previousChainLength).putInt(messageNumber).array()

    companion object {
        const val SIZE = X25519.KEY_BYTES + 2 * Int.SIZE_BYTES
    }
}
