package com.danyastry.e2echat.core.protocol.x3dh

import com.danyastry.e2echat.core.crypto.kdf.Hkdf

object X3dh {

    const val SHARED_SECRET_BYTES = 32
    private val INFO = "E2EChat X3DH".encodeToByteArray()
    private val PREFIX = ByteArray(32) { 0xFF.toByte() }
    private val SALT = ByteArray(32)

    fun deriveSecret(dhOutputs: List<ByteArray>): ByteArray =
        Hkdf.sha256(PREFIX + dhOutputs.reduce { acc, bytes -> acc + bytes }, SALT, INFO, SHARED_SECRET_BYTES)
}
