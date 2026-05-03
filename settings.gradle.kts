pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://ramani.jfrog.io/artifactory/maplibre-android") }
    }
}

rootProject.name = "Coding Challenge"
include(":app")
include(":data")
include(":domain")
include(":network")
include(":core")
// Feature modules — UI logic grouped by user-facing capability rather than layer.
// Currently scaffolded; Compose screens live in :app and will be migrated here incrementally.
include(":feature:characters")
include(":feature:favorites")
