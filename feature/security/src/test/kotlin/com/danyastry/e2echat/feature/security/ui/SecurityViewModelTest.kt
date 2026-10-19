package com.danyastry.e2echat.feature.security.ui

import com.danyastry.e2echat.feature.security.AttackerView
import com.danyastry.e2echat.feature.security.Delivery
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SecurityViewModelTest {

    private val viewModel = SecurityViewModel()

    private val state
        get() = viewModel.state.value

    @Test
    fun `starts on the forward secrecy demo with nothing done`() {
        assertThat(state).isEqualTo(SecurityUiState())
    }

    @Test
    fun `switching demos keeps what each demo has done`() {
        viewModel.onConverse()
        viewModel.onDemoChange(Demo.HEALING)
        viewModel.onDemoChange(Demo.FORWARD_SECRECY)

        assertThat(state.hasConversation).isTrue()
    }

    @Test
    fun `the thief cannot read any recorded message`() {
        viewModel.onConverse()
        viewModel.onStealAndAttack()

        val attack = state.stolenAttack!!
        assertThat(attack).hasSize(3)
        assertThat(attack.map { it.attackerRead }).containsExactly(null, null, null)
    }

    @Test
    fun `stealing before any conversation does nothing`() {
        viewModel.onStealAndAttack()

        assertThat(state.stolenAttack).isNull()
    }

    @Test
    fun `starting over clears the forward secrecy demo`() {
        viewModel.onConverse()
        viewModel.onStealAndAttack()
        viewModel.onResetForwardSecrecy()

        assertThat(state.hasConversation).isFalse()
        assertThat(state.stolenAttack).isNull()
    }

    @Test
    fun `the thief reads until user 2 replies and is locked out after`() {
        viewModel.onSteal()
        viewModel.onUser1Sends()
        viewModel.onUser2Replies()
        viewModel.onUser1Sends()

        assertThat(state.isStolen).isTrue()
        assertThat(state.timeline.map { it.view })
            .containsExactly(AttackerView.READ, AttackerView.NOT_TARGETED, AttackerView.LOCKED_OUT).inOrder()
    }

    @Test
    fun `starting over clears the break-in demo`() {
        viewModel.onSteal()
        viewModel.onUser1Sends()
        viewModel.onResetHealing()

        assertThat(state.isStolen).isFalse()
        assertThat(state.timeline).isEmpty()
    }

    @Test
    fun `messages delivered out of order are all read`() {
        viewModel.onSendThree()
        assertThat(state.heldCount).isEqualTo(3)

        viewModel.onDeliverOutOfOrder()

        assertThat(state.heldCount).isEqualTo(0)
        assertThat(state.deliveries.map { it.text }).containsExactly("Message 3", "Message 1", "Message 2").inOrder()
    }

    @Test
    fun `a replayed message and a forged message are rejected`() {
        viewModel.onSendThree()
        viewModel.onDeliverOutOfOrder()
        viewModel.onReplay()
        viewModel.onTamper()

        assertThat(state.deliveries.drop(3).map { it.delivery })
            .containsExactly(Delivery.REJECTED, Delivery.REJECTED, Delivery.READ).inOrder()
        assertThat(state.hasReplayed).isTrue()
        assertThat(state.hasTampered).isTrue()
    }

    @Test
    fun `replay and forgery wait until messages are delivered`() {
        viewModel.onSendThree()
        viewModel.onReplay()
        viewModel.onTamper()

        assertThat(state.deliveries).isEmpty()
    }

    @Test
    fun `sending three again starts the order demo over`() {
        viewModel.onSendThree()
        viewModel.onDeliverOutOfOrder()
        viewModel.onReplay()
        viewModel.onSendThree()

        assertThat(state.heldCount).isEqualTo(3)
        assertThat(state.deliveries).isEmpty()
        assertThat(state.hasReplayed).isFalse()
    }
}
