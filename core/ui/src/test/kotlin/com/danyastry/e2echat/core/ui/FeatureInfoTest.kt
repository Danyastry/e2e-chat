package com.danyastry.e2echat.core.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class FeatureInfoTest {

    @Test
    fun `accepts kebab-case ids`() {
        assertThat(info(id = "chat").id).isEqualTo("chat")
        assertThat(info(id = "double-ratchet").id).isEqualTo("double-ratchet")
    }

    @Test
    fun `rejects ids that are not lowercase kebab-case`() {
        listOf("", "X3DH", "double_ratchet", "double ratchet", "-x3dh", "x3dh-", "double--ratchet").forEach { id ->
            assertThrows(IllegalArgumentException::class.java) { info(id = id) }
        }
    }

    @Test
    fun `rejects blank names and minSdk below the baseline`() {
        assertThrows(IllegalArgumentException::class.java) { info(name = " ") }
        assertThrows(IllegalArgumentException::class.java) { info(minSdk = 25) }
    }

    @Test
    fun `defaults to the app baseline`() {
        assertThat(info().minSdk).isEqualTo(AndroidVersion.BASELINE_SDK)
    }

    @Test
    fun `is supported from its minSdk up`() {
        val ecdh = info(minSdk = 31)

        assertThat(ecdh.isSupportedOn(30)).isFalse()
        assertThat(ecdh.isSupportedOn(31)).isTrue()
        assertThat(ecdh.isSupportedOn(35)).isTrue()
    }

    private fun info(id: String = "chat", name: String = "Chat", minSdk: Int = AndroidVersion.BASELINE_SDK) =
        FeatureInfo(id = id, name = name, category = FeatureCategory.CHAT, summary = "Summary", minSdk = minSdk)
}
