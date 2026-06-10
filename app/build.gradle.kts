plugins {
    id("rainclass.android.application")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rainclass.app"
    defaultConfig {
        applicationId = "com.rainclass.app"
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}

dependencies {
    implementation(project(":core:config"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":feature:login"))
    implementation(project(":feature:home"))
    implementation(project(":feature:courses"))
    implementation(project(":feature:homework"))
    implementation(project(":feature:exam"))
    implementation(project(":feature:settings"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
}
