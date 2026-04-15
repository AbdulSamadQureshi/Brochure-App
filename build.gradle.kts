import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    jacoco
}

// ─── JaCoCo aggregate report ─────────────────────────────────────────────────
// Runs every module's testDebugUnitTest, then merges all .exec files into a
// single HTML + XML report at build/reports/jacoco/jacocoFullReport/.
jacoco {
    toolVersion = "0.8.12" // explicit version — supports Java 21 bytecode
}

val jacocoExcludes = listOf(
    // Android boilerplate
    "**/R.class", "**/R\$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    // Hilt / Dagger generated
    "**/hilt_aggregated_deps/**",
    "**/*_HiltModules.*", "**/*Hilt_*.*",
    "**/*_Factory.*", "**/*_MembersInjector.*", "**/*_GeneratedInjector.*",
    "**/*Module_Provide*",
    // Room generated
    "**/*Dao_Impl*", "**/*Database_Impl*", "**/*_Impl\$*",
    // Compose generated
    "**/ComposableSingletons*.*",
    // Test helpers
    "**/*Test*.*",
    // Hilt DI modules (hand-written but not unit-testable in isolation)
    "**/di/*Module*", "**/di/*Component*",
    // Compose UI screens / theme — covered by screenshot tests, not unit tests
    "**/presentation/theme/**",
    "**/presentation/detail/*Screen*",
    "**/presentation/home/*Screen*",
    "**/presentation/navigation/**",
    "**/core/ui/**",
    // Android entry points not unit-testable
    "**/MainActivity*",
)

tasks.register<JacocoReport>("jacocoFullReport") {
    group = "verification"
    description = "Generates an aggregated JaCoCo coverage report across all modules."

    // Depend on every module's debug unit-test task.
    dependsOn(subprojects.map { "${it.path}:testDebugUnitTest" })

    // .exec files written by AGP when enableUnitTestCoverage = true.
    executionData.setFrom(
        fileTree(rootDir) {
            include("**/build/outputs/unit_test_code_coverage/debugUnitTest/*.exec")
        },
    )

    // Compiled Kotlin production classes (AGP 9 path; excludes generated code).
    classDirectories.setFrom(
        files(
            subprojects.map { proj ->
                fileTree(
                    "${proj.layout.buildDirectory.get()}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes",
                ) {
                    exclude(jacocoExcludes)
                }
            },
        ),
    )

    // Human-readable source paths (so the HTML report links to actual .kt files).
    sourceDirectories.setFrom(
        files(
            subprojects.flatMap { proj ->
                listOf(
                    "${proj.projectDir}/src/main/java",
                    "${proj.projectDir}/src/main/kotlin",
                )
            }.filter { File(it).exists() },
        ),
    )

    reports {
        xml.required.set(true)   // consumed by Codecov / CI badge tools
        html.required.set(true)  // human-readable artifact
        csv.required.set(false)
    }

    doLast {
        val xmlReport = reports.xml.outputLocation.asFile.get()
        if (!xmlReport.exists()) return@doLast

        // Strip the DOCTYPE declaration before parsing — the JaCoCo DTD reference
        // triggers security restrictions in the JVM's SAX parser.
        val xmlContent = xmlReport.readText()
            .replace(Regex("<!DOCTYPE[^>]*>"), "")
        val doc = groovy.xml.XmlParser().parseText(xmlContent)
        val counters = (doc["counter"] as groovy.util.NodeList)
            .filterIsInstance<groovy.util.Node>()
            .associate { node ->
                node.attribute("type") as String to Pair(
                    (node.attribute("covered") as String).toInt(),
                    (node.attribute("missed") as String).toInt(),
                )
            }

        fun pct(type: String): String {
            val (covered, missed) = counters[type] ?: return "N/A"
            val total = covered + missed
            val p = if (total > 0) covered * 100.0 / total else 0.0
            val bar = "█".repeat((p / 10).toInt()) + "░".repeat(10 - (p / 10).toInt())
            return "$bar  ${"%.1f".format(p)}%  ($covered/$total)"
        }

        println("")
        println("┌─────────────────────────────────────────────┐")
        println("│           JaCoCo Coverage Report            │")
        println("├──────────────┬──────────────────────────────┤")
        println("│ Lines        │ ${pct("LINE").padEnd(28)} │")
        println("│ Instructions │ ${pct("INSTRUCTION").padEnd(28)} │")
        println("│ Branches     │ ${pct("BRANCH").padEnd(28)} │")
        println("│ Methods      │ ${pct("METHOD").padEnd(28)} │")
        println("│ Classes      │ ${pct("CLASS").padEnd(28)} │")
        println("└──────────────┴──────────────────────────────┘")
        println("  HTML report: ${reports.html.outputLocation.asFile.get()}/index.html")
        println("")
    }
}

// ─────────────────────────────────────────────────────────────────────────────

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    detekt {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
    }

    ktlint {
        android.set(true)
        ignoreFailures.set(false)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
    }

    // Set JVM toolchain for Kotlin
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper> {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
            jvmToolchain(21)
        }
    }

    // Explicitly set toolchain for any Java compilation tasks
    tasks.withType<JavaCompile>().configureEach {
        javaCompiler.set(project.extensions.getByType<JavaToolchainService>().compilerFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        })
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}
