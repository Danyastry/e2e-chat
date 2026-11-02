package com.danyastry.e2echat.core.protocol.pqxdh

import com.danyastry.e2echat.core.protocol.handshake.HandshakeKdf

object Pqxdh {

    const val KEM_KEY_TYPE: Byte = 0x08
    private val INFO = "E2EChat PQXDH".encodeToByteArray()

    fun encodeKemKey(publicKey: ByteArray): ByteArray = byteArrayOf(KEM_KEY_TYPE) + publicKey

    fun deriveSecret(dhOutputs: List<ByteArray>, kemSecret: ByteArray): ByteArray = HandshakeKdf.derive(INFO, dhOutputs, kemSecret)
}
