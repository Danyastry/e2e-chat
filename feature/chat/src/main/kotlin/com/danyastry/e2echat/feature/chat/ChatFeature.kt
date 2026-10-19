package com.danyastry.e2echat.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danyastry.e2echat.core.ui.FeatureCategory
import com.danyastry.e2echat.core.ui.FeatureEntry
import com.danyastry.e2echat.core.ui.FeatureInfo
import com.danyastry.e2echat.feature.chat.ui.ChatRoute

object ChatFeature : FeatureEntry {

    override val info = FeatureInfo(
        id = "chat",
        name = "Encrypted chat",
        category = FeatureCategory.CHAT,
        summary = "Two phones and a server that only sees ciphertext",
    )

    @Composable
    override fun Content(modifier: Modifier) {
        ChatRoute(modifier = modifier)
    }
}
