package com.danyastry.e2echat.core.protocol.pqxdh

import com.danyastry.e2echat.core.protocol.keys.PreKeyBundle

class PqxdhBundle(val classical: PreKeyBundle, val kemPreKey: SignedKemPreKey, val isLastResort: Boolean) {
    val size: Int
        get() = classical.size + kemPreKey.size
}
