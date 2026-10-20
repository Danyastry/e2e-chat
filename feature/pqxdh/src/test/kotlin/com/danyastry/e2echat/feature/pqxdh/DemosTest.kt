package com.danyastry.e2echat.feature.pqxdh

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DemosTest {

    @Test
    fun `records one chat over each handshake`() {
        val recordings = HarvestDemo().record()

        assertThat(recordings.map { it.handshake }).containsExactly(Handshake.X3DH, Handshake.PQXDH).inOrder()
        assertThat(recordings.map { it.messages }).containsExactly(HarvestDemo.MESSAGES, HarvestDemo.MESSAGES)
    }

    @Test
    fun `pqxdh adds the ml-kem key to the bundle and the ciphertext to the first message`() {
        val (x3dh, pqxdh) = HarvestDemo().record()

        assertThat(x3dh.bundleBytes).isEqualTo(200)
        assertThat(pqxdh.bundleBytes).isEqualTo(200 + 4 + 1568 + 64)
        assertThat(x3dh.headerBytes).isEqualTo(72)
        assertThat(pqxdh.headerBytes).isEqualTo(72 + 4 + 1568)
    }

    @Test
    fun `a quantum computer reads the x3dh chat but not the pqxdh chat`() {
        val demo = HarvestDemo().apply { record() }

        val (x3dh, pqxdh) = demo.attack()

        assertThat(x3dh.dhOutputsBroken).isEqualTo(4)
        assertThat(x3dh.attackerRead).isEqualTo(HarvestDemo.MESSAGES)
        assertThat(x3dh.isReadable).isTrue()
        assertThat(pqxdh.dhOutputsBroken).isEqualTo(4)
        assertThat(pqxdh.attackerRead).containsExactly(null, null)
        assertThat(pqxdh.isReadable).isFalse()
    }

    @Test
    fun `nothing to attack before anything is recorded`() {
        val demo = HarvestDemo()

        assertThat(demo.isRecorded).isFalse()
        assertThat(demo.attack()).isEmpty()
    }

    @Test
    fun `new chats use up the one-time ml-kem prekeys, then reuse the last-resort key`() {
        val demo = PreKeyDemo()

        val chats = List(5) { demo.startChat() }

        assertThat(chats.map { it.kemPreKeyId }).containsExactly(1, 2, 3, 0, 0).inOrder()
        assertThat(chats.map { it.isLastResort }).containsExactly(false, false, false, true, true).inOrder()
        assertThat(chats.map { it.oneTimeKemPreKeysLeft }).containsExactly(2, 1, 0, 0, 0).inOrder()
        assertThat(chats.all { it.agreed }).isTrue()
    }
}
