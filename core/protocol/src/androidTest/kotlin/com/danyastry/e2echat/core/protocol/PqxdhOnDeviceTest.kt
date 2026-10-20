package com.danyastry.e2echat.core.protocol

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PqxdhOnDeviceTest {

    @Test
    fun thePostQuantumHandshakeAgreesOnTheDevice() {
        val server = PqxdhKeyServer()
        val user1 = PqxdhUser("User 1")
        val user2 = PqxdhUser("User 2").also { server.publish("User 2", it.publishedKeys()) }

        val initiation = user1.initiate(server.fetchBundle("User 2"))

        assertThat(user2.respond(initiation.header).sharedSecret).isEqualTo(initiation.result.sharedSecret)
    }
}
