package com.danyastry.e2echat.feature.security

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danyastry.e2echat.core.ui.FeatureCategory
import com.danyastry.e2echat.core.ui.FeatureEntry
import com.danyastry.e2echat.core.ui.FeatureInfo
import com.danyastry.e2echat.feature.security.ui.SecurityRoute

object SecurityFeature : FeatureEntry {

    override val info = FeatureInfo(
        id = "security",
        name = "Stolen keys and lost messages",
        category = FeatureCategory.SECURITY,
        summary = "Forward secrecy, healing after a break-in, replay and reordering",
    )

    @Composable
    override fun Content(modifier: Modifier) {
        SecurityRoute(modifier = modifier)
    }
}
