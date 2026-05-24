import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.secrets)
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use(keystoreProperties::load)
}

val storeFileValue: String? =
    keystoreProperties.getProperty("storeFile") ?: System.getenv("STORE_FILE")
val storePasswordValue: String? =
    keystoreProperties.getProperty("storePassword") ?: System.getenv("STORE_PASSWORD")
val keyAliasValue: String? =
    keystoreProperties.getProperty("keyAlias") ?: System.getenv("KEY_ALIAS")
val keyPasswordValue: String? =
    keystoreProperties.getProperty("keyPassword") ?: System.getenv("KEY_PASSWORD")
val hasReleaseSigningConfig =
    storeFileValue != null && storePasswordValue != null && keyAliasValue != null && keyPasswordValue != null

android {
    namespace = "ca.gainzassist"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    defaultConfig {
        applicationId = "ca.gainzassist"
        minSdk = 21
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigningConfig) {
                storeFile = file(storeFileValue!!)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/INDEX.LIST"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

tasks.configureEach {
    if (name == "assembleRelease" || name == "bundleRelease") {
        doFirst {
            if (!hasReleaseSigningConfig) {
                throw GradleException("Release signing properties missing. Please provide storeFile, storePassword, keyAlias, and keyPassword in keystore.properties or via environment variables (STORE_FILE, STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD).")
            }
        }
    }
}

dependencies {
    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 1. Local files
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    // 2. Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation(libs.junit)

    // 3. Architecture Components (AndroidX)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    androidTestImplementation(libs.androidx.room.testing)

    implementation(libs.bundles.androidx.lifecycle)
    kapt(libs.androidx.lifecycle.compiler)

    // 4. AndroidX Core
    implementation(libs.bundles.androidx.core)

    // 5. Facebook
    implementation(libs.facebook.android.sdk)
    implementation(libs.facebook.rebound)

    // 6. Jackson
    implementation(libs.bundles.jackson)

    // 7. Google
    implementation(libs.bundles.google)

    // 8. Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // 9. Parceler
    implementation(libs.parceler.api)
    kapt(libs.parceler)

    // 10. UI / Logging
    implementation("com.google.guava:guava:33.2.1-android")
    implementation(libs.bundles.ui.logging)
    implementation(libs.android.youtube.player)
    implementation(libs.glide)
    kapt("com.github.bumptech.glide:compiler:4.16.0")
}

val validateReleaseSecrets by tasks.registering {
    group = "verification"
    description = "Validates required release secrets before building a release artifact."

    doLast {
        val secretsFile = rootProject.file("secrets.properties")

        if (!secretsFile.exists()) {
            throw GradleException(
                "Missing secrets.properties. Copy secrets.properties.template to secrets.properties " +
                    "and fill required release values before building release."
            )
        }

        val secrets = Properties().apply {
            secretsFile.inputStream().use { load(it) }
        }

        val isFacebookEnabled = secrets.getProperty("ENABLE_FACEBOOK_LOGIN")?.toBoolean() ?: false

        val requiredKeys = mutableListOf("GOOGLE_API_KEY")
        if (isFacebookEnabled) {
            requiredKeys.addAll(listOf(
                "FACEBOOK_APP_ID",
                "FACEBOOK_CLIENT_TOKEN",
                "FB_LOGIN_PROTOCOL_SCHEME"
            ))
        }

        val missingOrInvalid = requiredKeys.filter { key ->
            val value = secrets.getProperty(key) ?: return@filter true
            val trimValue = value.trim()
            trimValue.isEmpty() ||
                trimValue.contains("your_", ignoreCase = true) ||
                trimValue.contains("YOUR_", ignoreCase = true) ||
                trimValue.contains("template", ignoreCase = true) ||
                trimValue.contains("placeholder", ignoreCase = true)
        }

        if (missingOrInvalid.isNotEmpty()) {
            throw GradleException(
                "Invalid release secrets in secrets.properties. Missing or placeholder values for: " +
                    missingOrInvalid.joinToString(", ") +
                    (if (isFacebookEnabled) " (Note: Facebook login is ENABLED)" else "")
            )
        }
    }
}

tasks.matching {
    it.name in listOf(
        "assembleRelease",
        "bundleRelease"
    )
}.configureEach {
    dependsOn(validateReleaseSecrets)
}
