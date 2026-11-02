package com.danyastry.e2echat.core.crypto.kdf

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters

object Hkdf {

    fun sha256(inputKeyMaterial: ByteArray, salt: ByteArray, info: ByteArray, length: Int): ByteArray {
        val generator = HKDFBytesGenerator(SHA256Digest()).apply { init(HKDFParameters(inputKeyMaterial, salt, info)) }
        return ByteArray(length).also { generator.generateBytes(it, 0, length) }
    }
}
