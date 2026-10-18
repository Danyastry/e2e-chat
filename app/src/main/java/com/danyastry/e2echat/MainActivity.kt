package com.danyastry.e2echat

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.danyastry.e2echat.catalog.FeatureCatalog
import com.danyastry.e2echat.catalog.FeatureRegistry
import com.danyastry.e2echat.core.ui.theme.ChatTheme
import com.danyastry.e2echat.navigation.ChatNavHost

class MainActivity : ComponentActivity() {

    private val catalog = FeatureCatalog(FeatureRegistry.features, deviceSdk = Build.VERSION.SDK_INT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatTheme {
                ChatNavHost(catalog)
            }
        }
    }
}
