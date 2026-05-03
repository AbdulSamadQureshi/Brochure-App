import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * :feature:characters
 *
 * Owns everything the user sees when browsing and searching characters:
 *   - CharactersScreen (list + search bar)
 *   - CharacterDetailScreen
 *   - CharactersViewModel / CharacterDetailViewModel
 *   - UI models (CharacterUi, CharacterDetailUi)
 *   - Navigation routes for this feature
 *
 * Migration note: Compose screens and ViewModels currently live in :app under
 * `presentation/character` and `presentation/detail`. Moving them here is a
 * pure refactoring step that does not change any observable behaviour.
 * See SOLUTION.md § "Future work — feature modules" for the full migration plan.
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bonial.feature.characters"
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
    implementation(libs.paging.compose)
}
