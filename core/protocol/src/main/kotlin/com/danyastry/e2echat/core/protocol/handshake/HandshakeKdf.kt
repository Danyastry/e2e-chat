package com.danyastry.e2echat.core.protocol.handshake

import com.danyastry.e2echat.core.crypto.kdf.Hkdf

object HandshakeKdf {

    const val SECRET_BYTES = 32
    private val PREFIX = ByteArray(32) { 0xFF.toByte() }
    private val SALT = ByteArray(32)

    fun derive(info: ByteArray, dhOutputs: List<ByteArray>, kemSecret: ByteArray = ByteArray(0)): ByteArray =
        Hkdf.sha256(PREFIX + dhOutputs.reduce { acc, bytes -> acc + bytes } + kemSecret, SALT, info, SECRET_BYTES)
}
