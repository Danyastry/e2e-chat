package com.danyastry.e2echat.feature.ratchet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danyastry.e2echat.core.ui.FeatureCategory
import com.danyastry.e2echat.core.ui.FeatureEntry
import com.danyastry.e2echat.core.ui.FeatureInfo
import com.danyastry.e2echat.feature.ratchet.ui.RatchetRoute

object RatchetFeature : FeatureEntry {

    override val info = FeatureInfo(
        id = "double-ratchet",
        name = "Double Ratchet",
        category = FeatureCategory.PROTOCOL,
        summary = "A new key for every message, a new DH step for every reply",
    )

    @Composable
    override fun Content(modifier: Modifier) {
        RatchetRoute(modifier = modifier)
    }
}
