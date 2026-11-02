package com.danyastry.e2echat.feature.security.logic

import com.danyastry.e2echat.core.protocol.chat.Envelope
import com.danyastry.e2echat.feature.security.logic.Conversation.Companion.USER_1
import com.danyastry.e2echat.feature.security.logic.Conversation.Companion.USER_2
import com.danyastry.e2echat.feature.security.model.RecordedMessage

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
