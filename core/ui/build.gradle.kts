plugins {
    id("rainclass.android.library.compose")
}

android {
    namespace = "com.rainclass.core.ui"
}

dependencies {
    implementation(project(":core:designsystem"))
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.coil.compose)
}
