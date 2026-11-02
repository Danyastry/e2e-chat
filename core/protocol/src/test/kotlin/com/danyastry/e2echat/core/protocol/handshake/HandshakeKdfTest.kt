package com.danyastry.e2echat.core.protocol.handshake

import com.danyastry.e2echat.core.crypto.kdf.Hkdf
import com.danyastry.e2echat.core.protocol.pqxdh.Pqxdh
import com.danyastry.e2echat.core.protocol.x3dh.X3dh
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HandshakeKdfTest {

    private val outputs = List(4) { index -> ByteArray(32) { (index * 32 + it).toByte() } }
    private val kemSecret = ByteArray(32) { 0x5A }

    @Test
    fun `frames the input with 32 bytes of FF and a zero salt as the specs require`() {
        val info = "E2EChat X3DH".encodeToByteArray()
        val expected = Hkdf.sha256(ByteArray(32) { 0xFF.toByte() } + outputs.reduce { acc, bytes -> acc + bytes }, ByteArray(32), info, 32)

        assertThat(HandshakeKdf.derive(info, outputs)).isEqualTo(expected)
        assertThat(X3dh.deriveSecret(outputs)).isEqualTo(expected)
    }

    @Test
    fun `pqxdh appends the KEM secret and uses its own label`() {
        val withKem = Pqxdh.deriveSecret(outputs, kemSecret)

        assertThat(withKem).isEqualTo(HandshakeKdf.derive("E2EChat PQXDH".encodeToByteArray(), outputs, kemSecret))
        assertThat(withKem).isNotEqualTo(X3dh.deriveSecret(outputs))
        assertThat(Pqxdh.deriveSecret(outputs, ByteArray(32))).isNotEqualTo(withKem)
    }
}
