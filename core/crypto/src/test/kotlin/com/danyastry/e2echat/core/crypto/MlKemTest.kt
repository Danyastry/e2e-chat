package com.danyastry.e2echat.core.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class MlKemTest {

    @Test
    fun `keys, ciphertexts and secrets have the fips 203 sizes for ml-kem-1024`() {
        val keyPair = MlKem.generate()
        val encapsulation = MlKem.encapsulate(keyPair.publicKey)

        assertThat(keyPair.privateKey).hasLength(64)
        assertThat(keyPair.publicKey).hasLength(1568)
        assertThat(MlKem.expandedPrivateKey(keyPair.privateKey)).hasLength(3168)
        assertThat(encapsulation.ciphertext).hasLength(1568)
        assertThat(encapsulation.sharedSecret).hasLength(32)
    }

    @Test
    fun `the key holder gets the same secret out of the ciphertext`() {
        val keyPair = MlKem.generate()
        val encapsulation = MlKem.encapsulate(keyPair.publicKey)

        assertThat(MlKem.decapsulate(keyPair.privateKey, encapsulation.ciphertext)).isEqualTo(encapsulation.sharedSecret)
    }

    @Test
    fun `the same seed always gives the same key pair`() {
        val seed = ByteArray(64) { it.toByte() }

        assertThat(MlKem.fromSeed(seed).publicKey).isEqualTo(MlKem.fromSeed(seed).publicKey)
    }

    @Test
    fun `a different key gets a different secret instead of an error`() {
        val encapsulation = MlKem.encapsulate(MlKem.generate().publicKey)

        assertThat(MlKem.decapsulate(MlKem.generate().privateKey, encapsulation.ciphertext)).isNotEqualTo(encapsulation.sharedSecret)
    }

    @Test
    fun `a changed ciphertext gets a different secret instead of an error`() {
        val keyPair = MlKem.generate()
        val encapsulation = MlKem.encapsulate(keyPair.publicKey)
        val changed = encapsulation.ciphertext.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }

        assertThat(MlKem.decapsulate(keyPair.privateKey, changed)).isNotEqualTo(encapsulation.sharedSecret)
    }

    @Test
    fun `rejects inputs of the wrong size`() {
        val keyPair = MlKem.generate()

        assertThrows(IllegalArgumentException::class.java) { MlKem.fromSeed(ByteArray(32)) }
        assertThrows(IllegalArgumentException::class.java) { MlKem.encapsulate(ByteArray(1184)) }
        assertThrows(IllegalArgumentException::class.java) { MlKem.decapsulate(keyPair.privateKey, ByteArray(1088)) }
    }

    @Test
    fun `generates the nist acvp key pairs from their seeds`() {
        val cases = acvp("keyGen")
        assertThat(cases).hasSize(3)

        cases.forEach { case ->
            val seed = case.getValue("d") + case.getValue("z")

            assertThat(MlKem.fromSeed(seed).publicKey.toHex()).isEqualTo(case.getValue("ek").toHex())
            assertThat(MlKem.expandedPrivateKey(seed).toHex()).isEqualTo(case.getValue("dk").toHex())
        }
    }

    @Test
    fun `encapsulates to the nist acvp ciphertexts and secrets`() {
        val cases = acvp("encapsulation")
        assertThat(cases).hasSize(3)

        cases.forEach { case ->
            val encapsulation = MlKem.encapsulate(case.getValue("ek"), case.getValue("m"))

            assertThat(encapsulation.ciphertext.toHex()).isEqualTo(case.getValue("c").toHex())
            assertThat(encapsulation.sharedSecret.toHex()).isEqualTo(case.getValue("k").toHex())
        }
    }

    private fun acvp(kind: String): List<Map<String, ByteArray>> =
        javaClass.getResource("/ml-kem-1024-acvp.txt")!!.readText()
            .split("\n\n")
            .filter { it.startsWith("$kind ") }
            .map { block ->
                block.lines().drop(1).filter { " = " in it }.associate { it.substringBefore(" = ") to hex(it.substringAfter(" = ")) }
            }
}
