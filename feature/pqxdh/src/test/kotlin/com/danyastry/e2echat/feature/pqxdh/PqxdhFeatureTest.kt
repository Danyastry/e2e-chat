package com.danyastry.e2echat.feature.pqxdh

import com.danyastry.e2echat.core.ui.FeatureCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PqxdhFeatureTest {

    @Test
    fun `is listed under post-quantum and works on every supported version`() {
        assertThat(PqxdhFeature.info.id).isEqualTo("pqxdh")
        assertThat(PqxdhFeature.info.category).isEqualTo(FeatureCategory.POST_QUANTUM)
        assertThat(PqxdhFeature.info.minSdk).isEqualTo(26)
    }
}
