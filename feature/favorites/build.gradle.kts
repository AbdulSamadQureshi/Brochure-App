import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * :feature:favorites
 *
 * Owns everything the user sees in the Favourites tab:
 *   - FavouritesScreen (grid of favourited characters)
 *   - FavouritesViewModel
 *   - Navigation routes for this feature
 *
 * Migration note: Favourites logic is currently embedded in :app alongside
 * the characters feature. Extracting it here isolates the domain boundary
 * (FavouritesRepository, ToggleFavouriteUseCase) from unrelated UI code and
 * makes the module independently testable.
 * See SOLUTION.md § "Future work — feature modules" for the full migration plan.
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bonial.feature.favorites"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
}
