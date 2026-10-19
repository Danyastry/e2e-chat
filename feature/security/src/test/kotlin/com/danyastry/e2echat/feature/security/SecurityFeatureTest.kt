package com.danyastry.e2echat.feature.security

import com.danyastry.e2echat.core.ui.FeatureCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SecurityFeatureTest {

    @Test
    fun `is listed under security and works on every supported version`() {
        assertThat(SecurityFeature.info.id).isEqualTo("security")
        assertThat(SecurityFeature.info.category).isEqualTo(FeatureCategory.SECURITY)
        assertThat(SecurityFeature.info.minSdk).isEqualTo(26)
    }
}
