plugins {
  id("rainclass.android.library.compose")
}

android {
  namespace = "com.rainclass.core.config"
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  api(composeBom)
  api(libs.androidx.compose.material3)
  api(libs.androidx.compose.material.icons)
  api(libs.androidx.compose.ui)
  api(libs.androidx.compose.ui.tooling.preview)
  api(libs.androidx.compose.foundation)
  debugApi(libs.androidx.compose.ui.tooling)

  implementation(libs.androidx.datastore.preferences)
  implementation(libs.kotlinx.coroutines.core)
}
