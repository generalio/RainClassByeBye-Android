plugins {
    id("rainclass.android.library.compose")
}

android {
    namespace = "com.rainclass.feature.settings"
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
}
