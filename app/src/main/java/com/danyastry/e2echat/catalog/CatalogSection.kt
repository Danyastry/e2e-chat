package com.danyastry.e2echat.catalog

import com.danyastry.e2echat.core.ui.FeatureCategory
import com.danyastry.e2echat.core.ui.FeatureEntry

data class CatalogSection(
    val category: FeatureCategory,
    val entries: List<FeatureEntry>,
)
