plugins {
    id("rainclass.android.library.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rainclass.feature.homework"
}

dependencies {
    implementation(project(":core:config"))
    implementation(project(":core:network"))
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.koin.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
}
