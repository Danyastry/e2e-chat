package com.danyastry.e2echat.feature.pqxdh.logic

import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhKeyServer
import com.danyastry.e2echat.core.protocol.pqxdh.PqxdhUser
import com.danyastry.e2echat.feature.pqxdh.model.NewChat

class PreKeyDemo(private val oneTimeKemPreKeys: Int = PqxdhUser.DEFAULT_ONE_TIME_KEM_PREKEYS) {

    private val server = PqxdhKeyServer()
    private val user2 = PqxdhUser(HarvestDemo.USER_2, oneTimeKemPreKeyCount = oneTimeKemPreKeys)
        .also { server.publish(HarvestDemo.USER_2, it.publishedKeys()) }
    private var count = 0

    val oneTimeKemPreKeysLeft: Int
        get() = server.oneTimeKemPreKeysLeft(HarvestDemo.USER_2)

    fun startChat(): NewChat {
        val initiator = PqxdhUser("Contact ${++count}", oneTimePreKeyCount = 0, oneTimeKemPreKeyCount = 0)
        val bundle = server.fetchBundle(HarvestDemo.USER_2)
        val initiation = initiator.initiate(bundle)
        val agreed = user2.respond(initiation.header).sharedSecret.contentEquals(initiation.result.sharedSecret)
        return NewChat(count, bundle.kemPreKey.id, bundle.isLastResort, oneTimeKemPreKeysLeft, agreed)
    }
}
