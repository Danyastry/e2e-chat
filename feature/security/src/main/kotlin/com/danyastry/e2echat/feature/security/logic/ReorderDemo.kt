package com.danyastry.e2echat.feature.security.logic

import com.danyastry.e2echat.core.protocol.chat.Envelope
import com.danyastry.e2echat.core.protocol.chat.MessageEnvelope
import com.danyastry.e2echat.core.protocol.chat.PreKeyEnvelope
import com.danyastry.e2echat.core.protocol.ratchet.RatchetMessage
import com.danyastry.e2echat.feature.security.logic.Conversation.Companion.USER_1
import com.danyastry.e2echat.feature.security.logic.Conversation.Companion.USER_2
import com.danyastry.e2echat.feature.security.model.Delivery
import com.danyastry.e2echat.feature.security.model.DeliveryResult

class ReorderDemo {

    private val conversation = Conversation().apply { sendAndDeliver(USER_1, "Hello") }
    private val held = mutableListOf<Envelope>()

    val heldCount: Int
        get() = held.size

    fun sendThree() {
        held.clear()
        repeat(MESSAGES) { conversation.send(USER_1, "Message ${it + 1}") }
        held += conversation.takeWaiting(USER_2)
    }

    fun deliverOutOfOrder(): List<DeliveryResult> = DELIVERY_ORDER.map { index -> deliver(index, "Message ${index + 1}") }

    fun replay(): DeliveryResult = deliver(REPLAYED, "Message ${REPLAYED + 1} again")

    fun tamper(): List<DeliveryResult> {
        val envelope = conversation.send(USER_1, "Pay 100 dollars").also { conversation.takeWaiting(USER_2) }
        val ciphertext = envelope.message.ciphertext.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }
        val forged = envelope.withMessage(RatchetMessage(envelope.message.header, ciphertext))
        return listOf(result(forged, "Forged message"), result(envelope, "Original message"))
    }

    private fun deliver(index: Int, label: String): DeliveryResult = result(held[index], label)

    private fun result(envelope: Envelope, label: String): DeliveryResult {
        val text = runCatching { conversation.receive(USER_2, envelope) }.getOrNull()
        return DeliveryResult(label, if (text != null) Delivery.READ else Delivery.REJECTED, text)
    }

    private fun Envelope.withMessage(message: RatchetMessage): Envelope = when (this) {
        is PreKeyEnvelope -> PreKeyEnvelope(id, from, to, initial, message)
        is MessageEnvelope -> MessageEnvelope(id, from, to, message)
    }

    companion object {
        const val MESSAGES = 3
        const val REPLAYED = 1
        val DELIVERY_ORDER = listOf(2, 0, 1)
    }
}
