package com.danyastry.e2echat.feature.security

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DemosTest {

    @Test
    fun `a phone stolen today cannot read yesterday's messages`() {
        val demo = ForwardSecrecyDemo().apply { converse() }

        val attack = demo.stealAndAttack()

        assertThat(attack).hasSize(ForwardSecrecyDemo.ROUNDS)
        assertThat(attack.map { it.attackerRead }).containsExactly(null, null, null)
        assertThat(attack.map { it.text }).containsExactly("Message 1 from User 1", "Message 2 from User 1", "Message 3 from User 1").inOrder()
    }

    @Test
    fun `a thief reads new messages until the next round trip`() {
        val demo = HealingDemo()
        demo.steal()

        val whileInside = demo.user1Sends()
        val stillInside = demo.user1Sends()
        val reply = demo.user2Replies()
        val afterHealing = demo.user1Sends()

        assertThat(whileInside.view).isEqualTo(AttackerView.READ)
        assertThat(whileInside.attackerRead).isEqualTo(whileInside.text)
        assertThat(stillInside.view).isEqualTo(AttackerView.READ)
        assertThat(reply.view).isEqualTo(AttackerView.NOT_TARGETED)
        assertThat(afterHealing.view).isEqualTo(AttackerView.LOCKED_OUT)
        assertThat(afterHealing.attackerRead).isNull()
    }

    @Test
    fun `the thief stays locked out after healing`() {
        val demo = HealingDemo().apply { steal() }
        demo.user1Sends()
        demo.user2Replies()
        demo.user1Sends()

        val later = List(3) { demo.user1Sends() }

        assertThat(later.map { it.view }).containsExactly(AttackerView.LOCKED_OUT, AttackerView.LOCKED_OUT, AttackerView.LOCKED_OUT)
    }

    @Test
    fun `before the theft nothing is exposed`() {
        val demo = HealingDemo()

        assertThat(demo.user1Sends().view).isEqualTo(AttackerView.NOT_STOLEN_YET)
        assertThat(demo.isStolen).isFalse()
    }

    @Test
    fun `messages delivered as 3, 1, 2 are all read`() {
        val demo = ReorderDemo().apply { sendThree() }

        val results = demo.deliverOutOfOrder()

        assertThat(results.map { it.label }).containsExactly("Message 3", "Message 1", "Message 2").inOrder()
        assertThat(results.map { it.text }).containsExactly("Message 3", "Message 1", "Message 2").inOrder()
    }

    @Test
    fun `a replayed message is rejected`() {
        val demo = ReorderDemo().apply { sendThree() }
        demo.deliverOutOfOrder()

        assertThat(demo.replay().delivery).isEqualTo(Delivery.REJECTED)
    }

    @Test
    fun `a forged message is rejected and the real one still arrives`() {
        val demo = ReorderDemo().apply { sendThree() }
        demo.deliverOutOfOrder()

        val (forged, original) = demo.tamper()

        assertThat(forged.delivery).isEqualTo(Delivery.REJECTED)
        assertThat(original.delivery).isEqualTo(Delivery.READ)
        assertThat(original.text).isEqualTo("Pay 100 dollars")
    }
}
