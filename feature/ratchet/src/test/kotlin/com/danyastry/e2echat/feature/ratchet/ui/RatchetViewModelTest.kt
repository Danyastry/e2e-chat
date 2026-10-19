package com.danyastry.e2echat.feature.ratchet.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RatchetViewModelTest {

    private val viewModel = RatchetViewModel()

    private val state
        get() = viewModel.state.value

    @Test
    fun `starts right after x3dh, only user 1 can send`() {
        assertThat(state.user1?.sendingChain).isNotNull()
        assertThat(state.user2?.sendingChain).isNull()
        assertThat(state.canUser2Send).isFalse()
        assertThat(state.log).isEmpty()
    }

    @Test
    fun `user 2 reads what user 1 sent`() {
        viewModel.onInputChange("Meet at 9:45")

        viewModel.onSend(Participant.USER_1)

        assertThat(state.log.single().readText).isEqualTo("Meet at 9:45")
        assertThat(state.canUser2Send).isTrue()
    }

    @Test
    fun `user 2 cannot send first`() {
        viewModel.onSend(Participant.USER_2)

        assertThat(state.log).isEmpty()
    }

    @Test
    fun `messages in a row are chain steps with growing numbers`() {
        repeat(3) { viewModel.onSend(Participant.USER_1) }

        val entries = state.log.reversed()
        assertThat(entries.map { it.messageNumber }).containsExactly(0, 1, 2).inOrder()
        assertThat(entries.map { it.ratchetKey }.toSet()).hasSize(1)
        assertThat(entries.drop(1).map { it.receiverMadeDhStep }).containsExactly(false, false)
        assertThat(state.user1?.sent).isEqualTo(3)
        assertThat(state.user2?.received).isEqualTo(3)
    }

    @Test
    fun `the first message and every reply are dh steps`() {
        viewModel.onSend(Participant.USER_1)
        viewModel.onSend(Participant.USER_2)
        viewModel.onSend(Participant.USER_1)

        assertThat(state.log.map { it.receiverMadeDhStep }).containsExactly(true, true, true)
    }

    @Test
    fun `a reply carries the length of the previous chain`() {
        viewModel.onSend(Participant.USER_1)
        viewModel.onSend(Participant.USER_2)
        viewModel.onSend(Participant.USER_2)
        viewModel.onSend(Participant.USER_1)

        val reply = state.log.first()
        assertThat(reply.messageNumber).isEqualTo(0)
        assertThat(reply.previousChainLength).isEqualTo(1)
    }

    @Test
    fun `a reply changes the root key and the chains line up`() {
        viewModel.onSend(Participant.USER_1)
        val before = state.user1?.rootKey

        viewModel.onSend(Participant.USER_2)

        assertThat(state.user1?.rootKey).isNotEqualTo(before)
        assertThat(state.user1?.receivingChain).isEqualTo(state.user2?.sendingChain)
    }

    @Test
    fun `an empty message is not sent`() {
        viewModel.onInputChange("")

        viewModel.onSend(Participant.USER_1)

        assertThat(state.log).isEmpty()
    }

    @Test
    fun `a new session clears the log and keeps the text`() {
        viewModel.onInputChange("Hi")
        viewModel.onSend(Participant.USER_1)

        viewModel.onRestart()

        assertThat(state.log).isEmpty()
        assertThat(state.input).isEqualTo("Hi")
        assertThat(state.canUser2Send).isFalse()
    }
}
