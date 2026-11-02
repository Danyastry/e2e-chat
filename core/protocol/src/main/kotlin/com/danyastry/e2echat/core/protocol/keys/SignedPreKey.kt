package com.danyastry.e2echat.core.protocol.keys

import com.danyastry.e2echat.core.crypto.curve.KeyPair

class SignedPreKey(val id: Int, val keyPair: KeyPair, val signature: ByteArray)
