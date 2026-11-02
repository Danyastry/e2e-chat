package com.danyastry.e2echat.core.protocol.session

import com.danyastry.e2echat.core.protocol.keys.PreKeyBundle
import com.danyastry.e2echat.core.protocol.ratchet.DoubleRatchet
import com.danyastry.e2echat.core.protocol.x3dh.InitialHeader
import com.danyastry.e2echat.core.protocol.x3dh.X3dhResult
import com.danyastry.e2echat.core.protocol.x3dh.X3dhUser
import java.security.SecureRandom

fun X3dhUser.startSession(bundle: PreKeyBundle, random: SecureRandom = SecureRandom()): SessionStart<InitialHeader, X3dhResult> {
    val initiation = initiate(bundle)
    val session = DoubleRatchet.initiator(initiation.result.sharedSecret, bundle.signedPreKey, initiation.result.associatedData, random)
    return SessionStart(initiation.header, initiation.result, session)
}

fun X3dhUser.acceptSession(header: InitialHeader, random: SecureRandom = SecureRandom()): DoubleRatchet {
    val result = respond(header)
    return DoubleRatchet.responder(result.sharedSecret, signedPreKey.keyPair, result.associatedData, random)
}
