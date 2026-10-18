package com.danyastry.e2echat.feature.x3dh.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class X3dhViewModelTest {

    private val viewModel = X3dhViewModel()

    private val state
        get() = viewModel.state.value

    @Test
    fun `starts before user 2 has uploaded anything`() {
        assertThat(state.published).isNull()
        assertThat(state.canFetch).isFalse()
    }

    @Test
    fun `user 2 uploads a signed prekey and three one-time prekeys`() {
        viewModel.onPublish()

        assertThat(state.published?.signatureBytes).isEqualTo(64)
        assertThat(state.published?.oneTimePreKeys).isEqualTo(3)
        assertThat(state.serverOneTimeLeft).isEqualTo(3)
        assertThat(state.canFetch).isTrue()
    }

    @Test
    fun `a full handshake gives both users the same secret with four dh steps`() {
        handshake()

        assertThat(state.isSignatureValid).isTrue()
        assertThat(state.user1?.dhOutputs).hasSize(4)
        assertThat(state.secretsMatch).isTrue()
        assertThat(state.headerBytes).isEqualTo(72)
    }

    @Test
    fun `fetching takes one one-time prekey from the server`() {
        viewModel.onPublish()

        viewModel.onFetch()

        assertThat(state.bundle?.oneTimePreKeyId).isEqualTo(1)
        assertThat(state.serverOneTimeLeft).isEqualTo(2)
    }

    @Test
    fun `user 2 deletes the used one-time prekey`() {
        handshake()

        assertThat(state.user2OneTimeHeld).isEqualTo(2)
    }

    @Test
    fun `a swapped signed prekey stops user 1`() {
        viewModel.onPublish()
        viewModel.onMaliciousChange(true)
        viewModel.onFetch()

        viewModel.onInitiate()

        assertThat(state.bundle?.isForged).isTrue()
        assertThat(state.isSignatureValid).isFalse()
        assertThat(state.user1).isNull()
        assertThat(state.canRespond).isFalse()
        assertThat(state.canStartAgain).isTrue()
    }

    @Test
    fun `when one-time prekeys run out the handshake uses three dh steps`() {
        handshake()
        repeat(3) {
            viewModel.onStartAgain()
            viewModel.onFetch()
            viewModel.onInitiate()
            viewModel.onRespond()
        }

        assertThat(state.bundle?.oneTimePreKeyId).isNull()
        assertThat(state.user1?.dhOutputs).hasSize(3)
        assertThat(state.secretsMatch).isTrue()
        assertThat(state.handshakes).isEqualTo(4)
    }

    @Test
    fun `every handshake gives a new secret`() {
        handshake()
        val first = state.user1?.sharedSecret

        viewModel.onStartAgain()
        viewModel.onFetch()
        viewModel.onInitiate()

        assertThat(state.user1?.sharedSecret).isNotEqualTo(first)
    }

    @Test
    fun `starting over makes new users but keeps the server switch`() {
        handshake()
        viewModel.onMaliciousChange(true)

        viewModel.onPublish()

        assertThat(state.isServerMalicious).isTrue()
        assertThat(state.handshakes).isEqualTo(0)
        assertThat(state.bundle).isNull()
    }

    private fun handshake() {
        viewModel.onPublish()
        viewModel.onFetch()
        viewModel.onInitiate()
        viewModel.onRespond()
    }
}
