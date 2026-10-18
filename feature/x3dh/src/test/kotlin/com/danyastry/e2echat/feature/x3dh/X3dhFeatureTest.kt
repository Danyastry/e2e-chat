package com.danyastry.e2echat.feature.x3dh

import com.danyastry.e2echat.core.ui.FeatureCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class X3dhFeatureTest {

    @Test
    fun `is listed under protocol and works on every supported version`() {
        assertThat(X3dhFeature.info.id).isEqualTo("x3dh")
        assertThat(X3dhFeature.info.category).isEqualTo(FeatureCategory.PROTOCOL)
        assertThat(X3dhFeature.info.minSdk).isEqualTo(26)
    }
}
