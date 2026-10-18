plugins {
    alias(libs.plugins.e2echat.android.library.compose)
}

android {
    namespace = "com.danyastry.e2echat.core.ui"
}

dependencies {
    api(libs.androidx.material3)
}
