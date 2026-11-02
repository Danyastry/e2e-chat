package com.danyastry.e2echat.feature.pqxdh.logic

import com.danyastry.e2echat.core.crypto.curve.KeyPair
import com.danyastry.e2echat.core.protocol.ratchet.RatchetMessage
import com.danyastry.e2echat.feature.pqxdh.model.Recording

internal class RecordedSession(
    val recording: Recording,
    val ciphertexts: List<RatchetMessage>,
    val dhOutputs: List<ByteArray>,
    val associatedData: ByteArray,
    val responderRatchetKey: KeyPair,
)
