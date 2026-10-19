package com.danyastry.e2echat.feature.chat

import com.danyastry.e2echat.core.ui.FeatureCategory
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChatFeatureTest {

    @Test
    fun `is listed under chat and works on every supported version`() {
        assertThat(ChatFeature.info.id).isEqualTo("chat")
        assertThat(ChatFeature.info.category).isEqualTo(FeatureCategory.CHAT)
        assertThat(ChatFeature.info.minSdk).isEqualTo(26)
    }
}
