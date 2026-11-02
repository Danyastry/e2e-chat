package com.danyastry.e2echat.core.protocol.pqxdh

class SignedKemPreKey(val id: Int, val publicKey: ByteArray, val signature: ByteArray) {
    val size: Int
        get() = Int.SIZE_BYTES + publicKey.size + signature.size
}
