package com.danyastry.e2echat.core.protocol.pqxdh

import com.danyastry.e2echat.core.protocol.keys.PublishedKeys

class PqxdhPublishedKeys(
    val classical: PublishedKeys,
    val lastResortKemPreKey: SignedKemPreKey,
    val oneTimeKemPreKeys: List<SignedKemPreKey>,
)
