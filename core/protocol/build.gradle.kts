plugins {
    alias(libs.plugins.e2echat.android.library)
}

android {
    namespace = "com.danyastry.e2echat.core.protocol"
}

dependencies {
    api(projects.core.crypto)
}
