package com.danyastry.e2echat.core.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class KdfTest {

    @Test
    fun `hmac-sha256 matches rfc 4231 test case 2`() {
        val mac = Hmac.sha256("Jefe".encodeToByteArray(), "what do ya want for nothing?".encodeToByteArray())

        assertThat(mac.toHex()).isEqualTo("5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843")
    }

    @Test
    fun `hkdf-sha256 matches rfc 5869 test case 1`() {
        val okm = Hkdf.sha256(
            inputKeyMaterial = ByteArray(22) { 0x0b },
            salt = hex("000102030405060708090a0b0c"),
            info = hex("f0f1f2f3f4f5f6f7f8f9"),
            length = 42,
        )

        assertThat(okm.toHex()).isEqualTo("3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865")
    }
}
