package com.danyastry.e2echat.core.ui

import androidx.annotation.StringRes

@get:StringRes
val FeatureCategory.titleRes: Int
    get() = when (this) {
        FeatureCategory.CHAT -> R.string.category_chat
        FeatureCategory.PROTOCOL -> R.string.category_protocol
        FeatureCategory.SECURITY -> R.string.category_security
        FeatureCategory.POST_QUANTUM -> R.string.category_post_quantum
    }

@get:StringRes
val FeatureCategory.descriptionRes: Int
    get() = when (this) {
        FeatureCategory.CHAT -> R.string.category_chat_description
        FeatureCategory.PROTOCOL -> R.string.category_protocol_description
        FeatureCategory.SECURITY -> R.string.category_security_description
        FeatureCategory.POST_QUANTUM -> R.string.category_post_quantum_description
    }
