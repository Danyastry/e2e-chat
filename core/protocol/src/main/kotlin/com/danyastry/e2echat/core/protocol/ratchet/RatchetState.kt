package com.danyastry.e2echat.core.protocol.ratchet

import com.danyastry.e2echat.core.crypto.curve.KeyPair

internal class RatchetState(
    var ownRatchet: KeyPair,
    var remoteRatchet: ByteArray?,
    var rootKey: ByteArray,
    var sendingChain: ByteArray?,
    var receivingChain: ByteArray?,
    var sent: Int,
    var received: Int,
    var previousChainLength: Int,
    val skipped: LinkedHashMap<SkippedKey, ByteArray>,
) {
    fun copy() = RatchetState(ownRatchet, remoteRatchet, rootKey, sendingChain, receivingChain, sent, received, previousChainLength, LinkedHashMap(skipped))
}
