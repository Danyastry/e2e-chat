package com.danyastry.e2echat.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danyastry.e2echat.core.ui.FeatureCategory
import com.danyastry.e2echat.core.ui.FeatureEntry
import com.danyastry.e2echat.core.ui.FeatureInfo
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class FeatureCatalogTest {

    @Test
    fun `empty catalog has no sections`() {
        val catalog = FeatureCatalog(emptyList(), deviceSdk = 35)

        assertThat(catalog.isEmpty).isTrue()
        assertThat(catalog.sections).isEmpty()
    }

    @Test
    fun `groups features into sections ordered by category`() {
        val chat = feature("chat", "Chat", FeatureCategory.CHAT)
        val x3dh = feature("x3dh", "X3DH", FeatureCategory.PROTOCOL)
        val security = feature("forward-secrecy", "Forward secrecy", FeatureCategory.SECURITY)

        val catalog = FeatureCatalog(listOf(security, x3dh, chat), deviceSdk = 35)

        assertThat(catalog.sections.map { it.category }).containsExactly(
            FeatureCategory.CHAT,
            FeatureCategory.PROTOCOL,
            FeatureCategory.SECURITY,
        ).inOrder()
    }

    @Test
    fun `sorts features inside a section by name`() {
        val ratchet = feature("double-ratchet", "Double Ratchet", FeatureCategory.PROTOCOL)
        val x3dh = feature("x3dh", "X3DH", FeatureCategory.PROTOCOL)

        val catalog = FeatureCatalog(listOf(x3dh, ratchet), deviceSdk = 35)

        assertThat(catalog.sections.single().entries).containsExactly(ratchet, x3dh).inOrder()
    }

    @Test
    fun `features newer than the device are listed but not supported`() {
        val future = feature("future", "Future scheme", FeatureCategory.POST_QUANTUM, minSdk = 31)

        val catalog = FeatureCatalog(listOf(future), deviceSdk = 30)

        assertThat(catalog.sections.single().entries).containsExactly(future)
        assertThat(catalog.isSupported(future)).isFalse()
        assertThat(catalog.find("future")).isNull()
    }

    @Test
    fun `finds supported features by id`() {
        val future = feature("future", "Future scheme", FeatureCategory.POST_QUANTUM, minSdk = 31)

        val catalog = FeatureCatalog(listOf(future), deviceSdk = 31)

        assertThat(catalog.isSupported(future)).isTrue()
        assertThat(catalog.find("future")).isSameInstanceAs(future)
        assertThat(catalog.find("unknown")).isNull()
    }

    @Test
    fun `rejects duplicate ids`() {
        val first = feature("chat", "Chat", FeatureCategory.CHAT)
        val second = feature("chat", "Messages", FeatureCategory.CHAT)

        val error = assertThrows(IllegalArgumentException::class.java) { FeatureCatalog(listOf(first, second), deviceSdk = 35) }
        assertThat(error).hasMessageThat().contains("chat")
    }

    @Test
    fun `registry has unique ids`() {
        FeatureCatalog(FeatureRegistry.features, deviceSdk = 35)
    }

    private fun feature(id: String, name: String, category: FeatureCategory, minSdk: Int = 26): FeatureEntry =
        object : FeatureEntry {
            override val info = FeatureInfo(id = id, name = name, category = category, summary = "$name summary", minSdk = minSdk)

            @Composable
            override fun Content(modifier: Modifier) = Unit
        }
}
