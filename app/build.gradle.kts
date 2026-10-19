plugins {
    alias(libs.plugins.e2echat.android.application)
    alias(libs.plugins.e2echat.android.application.compose)
}

android {
    namespace = "com.danyastry.e2echat"

    defaultConfig {
        applicationId = "com.danyastry.e2echat"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(projects.core.ui)

    implementation(projects.feature.x3dh)
    implementation(projects.feature.ratchet)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material3)
}
