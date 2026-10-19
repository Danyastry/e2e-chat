package com.danyastry.e2echat.feature.ratchet

import com.danyastry.e2echat.core.ui.FeatureCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RatchetFeatureTest {

    @Test
    fun `is listed under protocol and works on every supported version`() {
        assertThat(RatchetFeature.info.id).isEqualTo("double-ratchet")
        assertThat(RatchetFeature.info.category).isEqualTo(FeatureCategory.PROTOCOL)
        assertThat(RatchetFeature.info.minSdk).isEqualTo(26)
    }
}
