plugins {
    id("rainclass.android.library")
}

android {
    namespace = "com.rainclass.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
}
