package com.danyastry.e2echat.catalog

import com.danyastry.e2echat.core.ui.FeatureEntry
import com.danyastry.e2echat.feature.chat.ChatFeature
import com.danyastry.e2echat.feature.ratchet.RatchetFeature
import com.danyastry.e2echat.feature.security.SecurityFeature
import com.danyastry.e2echat.feature.x3dh.X3dhFeature

object FeatureRegistry {
    val features: List<FeatureEntry> = listOf(
        ChatFeature,
        X3dhFeature,
        RatchetFeature,
        SecurityFeature,
    )
}
