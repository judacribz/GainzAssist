plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
//    apply(plugin = "dev.detekt")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**", "**/generated/**")
            ktlint("1.3.1").editorConfigOverride(
                mapOf(
                    "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                    "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_filename" to "disabled",
                    "ktlint_standard_max-line-length" to "120",
                    "ktlint_standard_multiline-expression-wrapping" to "disabled",
                    "ktlint_standard_binary-expression-wrapping" to "disabled",
                    "ktlint_standard_property-wrapping" to "disabled",
                    "ktlint_standard_parameter-list-wrapping" to "disabled",
                    "ktlint_standard_argument-list-wrapping" to "disabled",
                    "ktlint_standard_no-empty-first-line-in-method-block" to "disabled",
                    "ktlint_standard_package-name" to "disabled",
                    "ktlint_standard_property-naming" to "disabled",
                    "ktlint_standard_comment-wrapping" to "disabled",
                    "ktlint_standard_wrapping" to "disabled",
                    "ktlint_standard_function-signature" to "disabled",
                    "ktlint_standard_class-signature" to "disabled",
                    "ktlint_standard_function-expression-body" to "disabled"
                )
            )
        }
        kotlinGradle {
            target("**/*.kts")
            targetExclude("**/build/**", "**/generated/**")
            ktlint("1.3.1").editorConfigOverride(
                mapOf(
                    "ktlint_standard_max-line-length" to "120",
                    "ktlint_standard_multiline-expression-wrapping" to "disabled",
                    "ktlint_standard_binary-expression-wrapping" to "disabled",
                    "ktlint_standard_property-wrapping" to "disabled",
                    "ktlint_standard_parameter-list-wrapping" to "disabled",
                    "ktlint_standard_argument-list-wrapping" to "disabled",
                    "ktlint_standard_no-empty-first-line-in-method-block" to "disabled",
                    "ktlint_standard_package-name" to "disabled",
                    "ktlint_standard_property-naming" to "disabled",
                    "ktlint_standard_comment-wrapping" to "disabled",
                    "ktlint_standard_wrapping" to "disabled",
                    "ktlint_standard_function-signature" to "disabled",
                    "ktlint_standard_class-signature" to "disabled",
                    "ktlint_standard_function-expression-body" to "disabled"
                )
            )
        }
    }

//    dependencies {
//        add("detektPlugins", "io.nlopez.compose.rules:detekt:0.4.27")
//    }

    tasks.configureEach {
        if (name.contains("KotlinScripts")) enabled = false
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

tasks.register<Delete>("clean") {
    group = "build"
    description = "Delete the build directory."
    delete(rootProject.layout.buildDirectory)
}
