package com.danyastry.e2echat.feature.pqxdh

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danyastry.e2echat.core.ui.FeatureCategory
import com.danyastry.e2echat.core.ui.FeatureEntry
import com.danyastry.e2echat.core.ui.FeatureInfo
import com.danyastry.e2echat.feature.pqxdh.ui.PqxdhRoute

object PqxdhFeature : FeatureEntry {

    override val info = FeatureInfo(
        id = "pqxdh",
        name = "Post-quantum handshake",
        category = FeatureCategory.POST_QUANTUM,
        summary = "PQXDH adds ML-KEM-1024 so recorded chats stay private once quantum computers arrive",
    )

    @Composable
    override fun Content(modifier: Modifier) {
        PqxdhRoute(modifier = modifier)
    }
}
