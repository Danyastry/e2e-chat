package com.danyastry.e2echat.core.protocol

import com.danyastry.e2echat.core.crypto.Hkdf
import com.danyastry.e2echat.core.crypto.Hmac
import com.danyastry.e2echat.core.crypto.KeyPair
import com.danyastry.e2echat.core.crypto.MessageCipher
import com.danyastry.e2echat.core.crypto.X25519
import java.nio.ByteBuffer
import java.security.SecureRandom

class TooManySkippedMessagesException(skipped: Int) : Exception("Refusing to skip $skipped messages")

class MessageHeader(val ratchetKey: ByteArray, val previousChainLength: Int, val messageNumber: Int) {

    fun encode(): ByteArray = ByteBuffer.allocate(SIZE).put(ratchetKey).putInt(previousChainLength).putInt(messageNumber).array()

    companion object {
        const val SIZE = X25519.KEY_BYTES + 2 * Int.SIZE_BYTES
    }
}

class RatchetMessage(val header: MessageHeader, val ciphertext: ByteArray)

class RatchetSnapshot(
    val rootKey: ByteArray,
    val sendingChainKey: ByteArray?,
    val receivingChainKey: ByteArray?,
    val ownRatchetKey: ByteArray,
    val remoteRatchetKey: ByteArray?,
    val sent: Int,
    val received: Int,
    val previousChainLength: Int,
    val skippedKeys: Int,
)

private data class SkippedKey(val ratchetKey: List<Byte>, val messageNumber: Int)

private class RatchetState(
    var ownRatchet: KeyPair,
    var remoteRatchet: ByteArray?,
    var rootKey: ByteArray,
    var sendingChain: ByteArray?,
    var receivingChain: ByteArray?,
    var sent: Int,
    var received: Int,
    var previousChainLength: Int,
    val skipped: LinkedHashMap<SkippedKey, ByteArray>,
) {
    fun copy() = RatchetState(ownRatchet, remoteRatchet, rootKey, sendingChain, receivingChain, sent, received, previousChainLength, LinkedHashMap(skipped))
}

class DoubleRatchet private constructor(
    private var state: RatchetState,
    private val associatedData: ByteArray,
    private val random: SecureRandom,
) {

    fun encrypt(plaintext: ByteArray): RatchetMessage {
        val chain = checkNotNull(state.sendingChain) { "Cannot send before the first message from the other side" }
        val (nextChain, messageKey) = chainStep(chain)
        state.sendingChain = nextChain
        val header = MessageHeader(state.ownRatchet.publicKey, state.previousChainLength, state.sent)
        state.sent += 1
        return RatchetMessage(header, MessageCipher.encrypt(messageKey, plaintext, associatedData + header.encode()))
    }

    fun decrypt(message: RatchetMessage): ByteArray {
        val working = state.copy()
        val plaintext = decryptWith(working, message)
        state = working
        return plaintext
    }

    fun snapshot() = RatchetSnapshot(
        rootKey = state.rootKey,
        sendingChainKey = state.sendingChain,
        receivingChainKey = state.receivingChain,
        ownRatchetKey = state.ownRatchet.publicKey,
        remoteRatchetKey = state.remoteRatchet,
        sent = state.sent,
        received = state.received,
        previousChainLength = state.previousChainLength,
        skippedKeys = state.skipped.size,
    )

    private fun decryptWith(working: RatchetState, message: RatchetMessage): ByteArray {
        val header = message.header
        val ad = associatedData + header.encode()
        working.skipped.remove(SkippedKey(header.ratchetKey.toList(), header.messageNumber))?.let { key ->
            return MessageCipher.decrypt(key, message.ciphertext, ad)
        }
        if (!header.ratchetKey.contentEquals(working.remoteRatchet)) {
            skipMessageKeys(working, header.previousChainLength)
            dhRatchet(working, header)
        }
        skipMessageKeys(working, header.messageNumber)
        val (nextChain, messageKey) = chainStep(checkNotNull(working.receivingChain))
        working.receivingChain = nextChain
        working.received += 1
        return MessageCipher.decrypt(messageKey, message.ciphertext, ad)
    }

    private fun skipMessageKeys(working: RatchetState, until: Int) {
        if (working.received + MAX_SKIP < until) throw TooManySkippedMessagesException(until - working.received)
        val remote = working.remoteRatchet ?: return
        var chain = working.receivingChain ?: return
        while (working.received < until) {
            val (nextChain, messageKey) = chainStep(chain)
            working.skipped[SkippedKey(remote.toList(), working.received)] = messageKey
            chain = nextChain
            working.received += 1
        }
        working.receivingChain = chain
    }

    private fun dhRatchet(working: RatchetState, header: MessageHeader) {
        working.previousChainLength = working.sent
        working.sent = 0
        working.received = 0
        working.remoteRatchet = header.ratchetKey
        val (rootAfterReceive, receivingChain) = rootStep(working.rootKey, X25519.agree(working.ownRatchet.privateKey, header.ratchetKey))
        working.receivingChain = receivingChain
        working.ownRatchet = X25519.generate(random)
        val (rootAfterSend, sendingChain) = rootStep(rootAfterReceive, X25519.agree(working.ownRatchet.privateKey, header.ratchetKey))
        working.rootKey = rootAfterSend
        working.sendingChain = sendingChain
    }

    companion object {
        const val MAX_SKIP = 1_000
        private const val KEY_BYTES = 32
        private val ROOT_INFO = "E2EChat ratchet".encodeToByteArray()
        private val MESSAGE_KEY_INPUT = byteArrayOf(0x01)
        private val CHAIN_KEY_INPUT = byteArrayOf(0x02)

        fun initiator(sharedSecret: ByteArray, remoteRatchetKey: ByteArray, associatedData: ByteArray, random: SecureRandom = SecureRandom()): DoubleRatchet {
            val own = X25519.generate(random)
            val (rootKey, sendingChain) = rootStep(sharedSecret, X25519.agree(own.privateKey, remoteRatchetKey))
            return DoubleRatchet(RatchetState(own, remoteRatchetKey, rootKey, sendingChain, null, 0, 0, 0, LinkedHashMap()), associatedData, random)
        }

        fun responder(sharedSecret: ByteArray, ownRatchetKey: KeyPair, associatedData: ByteArray, random: SecureRandom = SecureRandom()): DoubleRatchet =
            DoubleRatchet(RatchetState(ownRatchetKey, null, sharedSecret, null, null, 0, 0, 0, LinkedHashMap()), associatedData, random)

        internal fun rootStep(rootKey: ByteArray, dhOutput: ByteArray): Pair<ByteArray, ByteArray> {
            val derived = Hkdf.sha256(dhOutput, rootKey, ROOT_INFO, KEY_BYTES * 2)
            return derived.copyOfRange(0, KEY_BYTES) to derived.copyOfRange(KEY_BYTES, KEY_BYTES * 2)
        }

        internal fun chainStep(chainKey: ByteArray): Pair<ByteArray, ByteArray> =
            Hmac.sha256(chainKey, CHAIN_KEY_INPUT) to Hmac.sha256(chainKey, MESSAGE_KEY_INPUT)
    }
}
