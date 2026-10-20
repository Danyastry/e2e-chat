package com.danyastry.e2echat.feature.pqxdh.ui

import com.danyastry.e2echat.feature.pqxdh.Handshake
import com.danyastry.e2echat.feature.pqxdh.HarvestDemo
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PqxdhViewModelTest {

    private val viewModel = PqxdhViewModel()

    private val state
        get() = viewModel.state.value

    @Test
    fun `starts with nothing recorded and three one-time ml-kem prekeys`() {
        assertThat(state.recordings).isEmpty()
        assertThat(state.attacks).isEmpty()
        assertThat(state.chats).isEmpty()
        assertThat(state.oneTimeKemPreKeysLeft).isEqualTo(3)
    }

    @Test
    fun `recording shows both handshakes`() {
        viewModel.onRecord()

        assertThat(state.recordings.map { it.handshake }).containsExactly(Handshake.X3DH, Handshake.PQXDH).inOrder()
    }

    @Test
    fun `the attack reads x3dh and fails on pqxdh`() {
        viewModel.onRecord()
        viewModel.onAttack()

        val (x3dh, pqxdh) = state.attacks
        assertThat(x3dh.attackerRead).isEqualTo(HarvestDemo.MESSAGES)
        assertThat(pqxdh.attackerRead).containsExactly(null, null)
    }

    @Test
    fun `the attack waits until something is recorded`() {
        viewModel.onAttack()

        assertThat(state.attacks).isEmpty()
    }

    @Test
    fun `starting over clears the recording and the attack`() {
        viewModel.onRecord()
        viewModel.onAttack()
        viewModel.onResetHarvest()

        assertThat(state.recordings).isEmpty()
        assertThat(state.attacks).isEmpty()
    }

    @Test
    fun `each new chat takes a one-time ml-kem prekey until only the last-resort key is left`() {
        repeat(4) { viewModel.onStartChat() }

        assertThat(state.chats.map { it.isLastResort }).containsExactly(false, false, false, true).inOrder()
        assertThat(state.oneTimeKemPreKeysLeft).isEqualTo(0)
    }

    @Test
    fun `starting over gives user 2 fresh one-time ml-kem prekeys`() {
        repeat(2) { viewModel.onStartChat() }
        viewModel.onResetPreKeys()

        assertThat(state.chats).isEmpty()
        assertThat(state.oneTimeKemPreKeysLeft).isEqualTo(3)
    }
}
