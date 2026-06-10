plugins {
    id("rainclass.android.application")
    alias(libs.plugins.kotlin.serialization)
}

val releaseVersionCode = providers.environmentVariable("VERSION_CODE")
    .map(String::toInt)
    .orElse(1)
val releaseVersionName = providers.environmentVariable("VERSION_NAME")
    .orElse("1.0")
val releaseStoreFile = providers.environmentVariable("ANDROID_KEYSTORE_PATH")
val releaseStorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD")
val releaseKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS")
val releaseKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD")
val hasReleaseSigning = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { it.isPresent }

android {
    namespace = "com.rainclass.app"
    defaultConfig {
        applicationId = "com.rainclass.app"
        versionCode = releaseVersionCode.get()
        versionName = releaseVersionName.get()
    }
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFile.get())
                storePassword = releaseStorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}

dependencies {
    implementation(project(":core:config"))
    implementation(project(":core:navigation3"))
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
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
}
