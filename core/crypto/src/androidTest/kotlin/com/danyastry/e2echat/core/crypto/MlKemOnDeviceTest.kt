package com.danyastry.e2echat.core.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MlKemOnDeviceTest {

    @Test
    fun bothSidesGetTheSameSecretOnTheDevice() {
        val keyPair = MlKem.generate()
        val encapsulation = MlKem.encapsulate(keyPair.publicKey)

        assertThat(MlKem.decapsulate(keyPair.privateKey, encapsulation.ciphertext)).isEqualTo(encapsulation.sharedSecret)
    }
}
