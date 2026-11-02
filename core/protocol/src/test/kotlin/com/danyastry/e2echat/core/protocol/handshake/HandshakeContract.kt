package com.danyastry.e2echat.core.protocol.handshake

import com.danyastry.e2echat.core.protocol.session.acceptSession
import com.danyastry.e2echat.core.protocol.session.startSession
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

abstract class HandshakeContract<Bundle : HandshakeBundle, Header : HandshakeHeader> {

    abstract fun directory(): KeyDirectory<Bundle, Header>

    abstract fun foreignHeader(): HandshakeHeader

    abstract val usesKem: Boolean

    private lateinit var directory: KeyDirectory<Bundle, Header>
    private lateinit var user1: Handshake<Bundle, Header>
    private lateinit var user2: Handshake<Bundle, Header>

    @Before
    fun setUp() {
        directory = directory()
        user1 = directory.register("User 1")
        user2 = directory.register("User 2")
    }

    @Test
    fun `both sides derive the same secret and associated data`() {
        val start = user1.initiate(directory.fetchBundle("User 2"))
        val result = user2.respond(start.header)

        assertThat(result.sharedSecret).isEqualTo(start.result.sharedSecret)
        assertThat(result.associatedData).isEqualTo(start.result.associatedData)
        assertThat(start.result.sharedSecret).hasLength(HandshakeKdf.SECRET_BYTES)
    }

    @Test
    fun `the transcript shows the four DH outputs and the KEM secret only when there is one`() {
        val start = user1.initiate(directory.fetchBundle("User 2"))

        assertThat(start.result.transcript.dhOutputs).hasSize(4)
        assertThat(start.result.transcript.kemSecret != null).isEqualTo(usesKem)
    }

    @Test
    fun `a header from the other handshake is not accepted`() {
        val own = user1.initiate(directory.fetchBundle("User 2")).header

        assertThat(user2.headerOf(own)).isSameInstanceAs(own)
        assertThat(user2.headerOf(foreignHeader())).isNull()
    }

    @Test
    fun `the session on top works in both directions`() {
        val start = user1.startSession(directory.fetchBundle("User 2"))
        val accepted = user2.acceptSession(start.header)

        assertThat(accepted.decrypt(start.session.encrypt("ping".encodeToByteArray())).decodeToString()).isEqualTo("ping")
        assertThat(start.session.decrypt(accepted.encrypt("pong".encodeToByteArray())).decodeToString()).isEqualTo("pong")
    }
}
