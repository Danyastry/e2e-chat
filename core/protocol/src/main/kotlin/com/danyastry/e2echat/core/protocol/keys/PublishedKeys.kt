package com.danyastry.e2echat.core.protocol.keys

class PublishedKeys(
    val identityKey: ByteArray,
    val signingKey: ByteArray,
    val signedPreKeyId: Int,
    val signedPreKey: ByteArray,
    val signedPreKeySignature: ByteArray,
    val oneTimePreKeys: Map<Int, ByteArray>,
)
