package com.danyastry.e2echat.core.protocol.pqxdh

import com.danyastry.e2echat.core.crypto.kdf.Hkdf
import com.danyastry.e2echat.core.protocol.x3dh.X3dh

object Pqxdh {

    const val KEM_KEY_TYPE: Byte = 0x08
    private val INFO = "E2EChat PQXDH".encodeToByteArray()
    private val PREFIX = ByteArray(32) { 0xFF.toByte() }
    private val SALT = ByteArray(32)

    fun encodeKemKey(publicKey: ByteArray): ByteArray = byteArrayOf(KEM_KEY_TYPE) + publicKey

    fun deriveSecret(dhOutputs: List<ByteArray>, kemSecret: ByteArray): ByteArray =
        Hkdf.sha256(PREFIX + dhOutputs.reduce { acc, bytes -> acc + bytes } + kemSecret, SALT, INFO, X3dh.SHARED_SECRET_BYTES)
}
