package com.danyastry.e2echat.feature.security.logic

import com.danyastry.e2echat.core.protocol.conversation.Conversation
import com.danyastry.e2echat.core.protocol.conversation.Participants.USER_1
import com.danyastry.e2echat.core.protocol.conversation.Participants.USER_2
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet
import com.danyastry.e2echat.feature.security.model.AttackerView
import com.danyastry.e2echat.feature.security.model.TimelineEntry

class HealingDemo {

    private val conversation = Conversation.x3dh().apply {
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
