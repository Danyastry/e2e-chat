package com.danyastry.e2echat.core.protocol.session

import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhBundle
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhHeader
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhResult
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhUser
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet
import java.security.SecureRandom

fun PqxdhUser.startSession(bundle: PqxdhBundle, random: SecureRandom = SecureRandom()): SessionStart<PqxdhHeader, PqxdhResult> {
    val initiation = initiate(bundle)
    val session = DoubleRatchet.initiator(initiation.result.sharedSecret, bundle.classical.signedPreKey, initiation.result.associatedData, random)
    return SessionStart(initiation.header, initiation.result, session)
}

fun PqxdhUser.acceptSession(header: PqxdhHeader, random: SecureRandom = SecureRandom()): DoubleRatchet {
    val result = respond(header)
    return DoubleRatchet.responder(result.sharedSecret, signedPreKey.keyPair, result.associatedData, random)
}
