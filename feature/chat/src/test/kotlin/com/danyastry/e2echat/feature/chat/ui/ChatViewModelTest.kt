package com.danyastry.e2echat.feature.chat.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChatViewModelTest {

    private val viewModel = ChatViewModel()

    private val state
        get() = viewModel.state.value

    @Test
    fun `starts with empty chats and nothing on the server`() {
        assertThat(state.user1).isEmpty()
        assertThat(state.user2).isEmpty()
        assertThat(state.server).isEmpty()
        assertThat(state.isUser2Online).isTrue()
    }

    @Test
    fun `user 2 reads what user 1 sends`() {
        send(Person.USER_1, "Meet me at 9:45")

        assertThat(state.user1).containsExactly(Bubble("Meet me at 9:45", isMine = true))
        assertThat(state.user2).containsExactly(Bubble("Meet me at 9:45", isMine = false))
        assertThat(state.user1Input).isEmpty()
    }

    @Test
    fun `a reply goes the other way`() {
        send(Person.USER_1, "Hi")
        send(Person.USER_2, "Hi back")

        assertThat(state.user1.map { it.text }).containsExactly("Hi", "Hi back").inOrder()
        assertThat(state.user1.last().isMine).isFalse()
    }

    @Test
    fun `the first message runs x3dh and later ones do not`() {
        send(Person.USER_1, "Hi")
        send(Person.USER_2, "Hi back")
        send(Person.USER_1, "How are you?")

        assertThat(state.server.map { it.kind }).containsExactly(EnvelopeKind.MESSAGE, EnvelopeKind.MESSAGE, EnvelopeKind.PRE_KEY).inOrder()
    }

    @Test
    fun `the server never sees the text`() {
        send(Person.USER_1, "bridge")

        val entry = state.server.single()
        assertThat(entry.preview).doesNotContain("bridge")
        assertThat(entry.bytes).isGreaterThan(0)
    }

    @Test
    fun `messages wait on the server while user 2 is offline`() {
        viewModel.onOnlineChange(false)

        send(Person.USER_1, "one")
        send(Person.USER_1, "two")

        assertThat(state.user2).isEmpty()
        assertThat(state.waitingForUser2).isEqualTo(2)
        assertThat(state.server.all { it.isWaiting }).isTrue()
    }

    @Test
    fun `coming back online delivers the waiting messages in order`() {
        viewModel.onOnlineChange(false)
        send(Person.USER_1, "one")
        send(Person.USER_1, "two")

        viewModel.onOnlineChange(true)

        assertThat(state.user2.map { it.text }).containsExactly("one", "two").inOrder()
        assertThat(state.waitingForUser2).isEqualTo(0)
    }

    @Test
    fun `user 2 cannot send while offline`() {
        send(Person.USER_1, "Hi")
        viewModel.onOnlineChange(false)

        send(Person.USER_2, "Hi back")

        assertThat(state.user2.map { it.text }).containsExactly("Hi")
        assertThat(state.user1).hasSize(1)
    }

    @Test
    fun `blank messages are not sent`() {
        send(Person.USER_1, "   ")

        assertThat(state.server).isEmpty()
    }

    private fun send(person: Person, text: String) {
        viewModel.onInputChange(person, text)
        viewModel.onSend(person)
    }
}
