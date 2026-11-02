package com.danyastry.e2echat.core.protocol.x3dh

import com.danyastry.e2echat.core.protocol.handshake.HandshakeKdf

object X3dh {

    const val SHARED_SECRET_BYTES = HandshakeKdf.SECRET_BYTES
    private val INFO = "E2EChat X3DH".encodeToByteArray()

    fun deriveSecret(dhOutputs: List<ByteArray>): ByteArray = HandshakeKdf.derive(INFO, dhOutputs)
}
