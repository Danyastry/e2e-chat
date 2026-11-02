package com.danyastry.e2echat.core.protocol.ratchet

internal data class SkippedKey(val ratchetKey: List<Byte>, val messageNumber: Int)
