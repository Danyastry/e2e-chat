package com.danyastry.e2echat.feature.security

import com.danyastry.e2echat.core.protocol.DoubleRatchet
import com.danyastry.e2echat.core.protocol.Envelope
import com.danyastry.e2echat.core.protocol.MessageEnvelope
import com.danyastry.e2echat.core.protocol.PreKeyEnvelope
import com.danyastry.e2echat.core.protocol.RatchetMessage
import com.danyastry.e2echat.feature.security.Conversation.Companion.USER_1
import com.danyastry.e2echat.feature.security.Conversation.Companion.USER_2

data class RecordedMessage(val number: Int, val text: String, val attackerRead: String?)

class ForwardSecrecyDemo {

    private val conversation = Conversation()
    private val recorded = mutableListOf<Pair<Envelope, String>>()

    var hasConversation = false
        private set

    fun converse(rounds: Int = ROUNDS) {
        repeat(rounds) { round ->
            val text = "Message ${round + 1} from User 1"
            recorded += conversation.sendAndDeliver(USER_1, text) to text
            conversation.sendAndDeliver(USER_2, "Reply ${round + 1} from User 2")
        }
        hasConversation = true
    }

    fun stealAndAttack(): List<RecordedMessage> {
        val stolen = conversation.stealSession(USER_2)
        return recorded.mapIndexed { index, (envelope, text) -> RecordedMessage(index + 1, text, stolen.tryRead(envelope)) }
    }

    companion object {
        const val ROUNDS = 3
    }
}

enum class AttackerView { NOT_STOLEN_YET, READ, LOCKED_OUT, NOT_TARGETED }

data class TimelineEntry(val from: String, val text: String, val view: AttackerView, val attackerRead: String?)

class HealingDemo {

    private val conversation = Conversation().apply {
        sendAndDeliver(USER_1, "Hello")
        sendAndDeliver(USER_2, "Hi")
    }
    private var stolen: DoubleRatchet? = null
    private var count = 0

    val isStolen: Boolean
        get() = stolen != null

    fun steal() {
        stolen = conversation.stealSession(USER_2)
    }

    fun user1Sends(): TimelineEntry {
        val text = "User 1 message ${++count}"
        val envelope = conversation.sendAndDeliver(USER_1, text)
        val attacker = stolen ?: return TimelineEntry(USER_1, text, AttackerView.NOT_STOLEN_YET, null)
        val read = attacker.tryRead(envelope)
        return TimelineEntry(USER_1, text, if (read != null) AttackerView.READ else AttackerView.LOCKED_OUT, read)
    }

    fun user2Replies(): TimelineEntry {
        val text = "User 2 reply ${++count}"
        conversation.sendAndDeliver(USER_2, text)
        return TimelineEntry(USER_2, text, if (stolen == null) AttackerView.NOT_STOLEN_YET else AttackerView.NOT_TARGETED, null)
    }
}

enum class Delivery { READ, REJECTED }

data class DeliveryResult(val label: String, val delivery: Delivery, val text: String?)

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
