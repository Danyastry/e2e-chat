package com.danyastry.e2echat.feature.x3dh

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danyastry.e2echat.core.ui.FeatureCategory
import com.danyastry.e2echat.core.ui.FeatureEntry
import com.danyastry.e2echat.core.ui.FeatureInfo
import com.danyastry.e2echat.feature.x3dh.ui.X3dhRoute

object X3dhFeature : FeatureEntry {

    override val info = FeatureInfo(
        id = "x3dh",
        name = "X3DH handshake",
        category = FeatureCategory.PROTOCOL,
        summary = "Start a secure chat with someone who is offline",
    )

    @Composable
    override fun Content(modifier: Modifier) {
        X3dhRoute(modifier = modifier)
    }
}
