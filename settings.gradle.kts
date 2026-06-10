pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "RainClass"

include(":app")
include(":core:config")
include(":core:navigation3")
include(":core:network")
include(":core:database")
include(":feature:login")
include(":feature:home")
include(":feature:courses")
include(":feature:homework")
include(":feature:exam")
include(":feature:settings")
