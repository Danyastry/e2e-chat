package com.danyastry.e2echat.core.crypto.kdf

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter

object Hmac {

    const val SHA256_BYTES = 32

    fun sha256(key: ByteArray, data: ByteArray): ByteArray = HMac(SHA256Digest()).run {
        init(KeyParameter(key))
        update(data, 0, data.size)
        ByteArray(SHA256_BYTES).also { doFinal(it, 0) }
    }
}
