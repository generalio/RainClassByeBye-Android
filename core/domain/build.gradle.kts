plugins {
    id("rainclass.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rainclass.core.domain"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
}
