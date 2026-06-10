import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.android.application")
      pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

      extensions.configure<ApplicationExtension> {
        compileSdk = 36
        defaultConfig {
          minSdk = 26
          targetSdk = 36
        }
        compileOptions {
          sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
          targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
        }
        buildFeatures {
          compose = true
        }
        packaging {
          resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
          }
        }
      }

      extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension> {
        jvmToolchain(17)
      }
    }
  }
}
