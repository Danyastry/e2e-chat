package com.danyastry.e2echat.core.protocol.pqxdh

import com.danyastry.e2echat.core.protocol.keys.KeyServer

class PqxdhKeyServer {

    private val classical = KeyServer()
    private val lastResort = mutableMapOf<String, SignedKemPreKey>()
    private val oneTime = mutableMapOf<String, ArrayDeque<SignedKemPreKey>>()

    fun publish(user: String, keys: PqxdhPublishedKeys) {
        classical.publish(user, keys.classical)
        lastResort[user] = keys.lastResortKemPreKey
        oneTime[user] = ArrayDeque(keys.oneTimeKemPreKeys.sortedBy { it.id })
    }

    fun fetchBundle(user: String): PqxdhBundle {
        val bundle = classical.fetchBundle(user)
        val oneTimeKey = oneTime.getValue(user).removeFirstOrNull()
        return PqxdhBundle(bundle, oneTimeKey ?: lastResort.getValue(user), isLastResort = oneTimeKey == null)
    }

    fun oneTimePreKeysLeft(user: String): Int = classical.oneTimePreKeysLeft(user)

    fun oneTimeKemPreKeysLeft(user: String): Int = oneTime[user]?.size ?: 0
}
