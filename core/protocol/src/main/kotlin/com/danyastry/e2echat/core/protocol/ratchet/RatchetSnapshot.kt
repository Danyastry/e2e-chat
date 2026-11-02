package com.danyastry.e2echat.core.protocol.ratchet

class RatchetSnapshot(
    val rootKey: ByteArray,
    val sendingChainKey: ByteArray?,
    val receivingChainKey: ByteArray?,
    val ownRatchetKey: ByteArray,
    val remoteRatchetKey: ByteArray?,
    val sent: Int,
    val received: Int,
    val previousChainLength: Int,
    val skippedKeys: Int,
)
