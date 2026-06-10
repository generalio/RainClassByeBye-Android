plugins {
  `kotlin-dsl`
}

dependencies {
  compileOnly(libs.android.gradle.plugin)
  compileOnly(libs.kotlin.gradle.plugin)
  compileOnly(libs.compose.gradle.plugin)
}

gradlePlugin {
  plugins {
    register("androidApplication") {
      id = "rainclass.android.application"
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidLibrary") {
      id = "rainclass.android.library"
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidLibraryCompose") {
      id = "rainclass.android.library.compose"
      implementationClass = "AndroidLibraryComposeConventionPlugin"
    }
    register("jvmLibrary") {
      id = "rainclass.jvm.library"
      implementationClass = "JvmLibraryConventionPlugin"
    }
  }
}
