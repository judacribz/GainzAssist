import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.secrets)
    alias(libs.plugins.gainzassist.formatting)
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
val hasReleaseSigningConfig = storeFileValue != null &&
    storePasswordValue != null &&
    keyAliasValue != null &&
    keyPasswordValue != null

configure<com.android.build.api.dsl.ApplicationExtension> {
    namespace = "ca.gainzassist"
    //noinspection GradleDependency
    compileSdk = 37

    buildFeatures {
        buildConfig = true
        compose = true
    }

    defaultConfig {
        applicationId = "ca.gainzassist"
        minSdk = 23
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 4
        versionName = "2606.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigningConfig) {
                storeFile = project.file(storeFileValue!!)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
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

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.configureEach {
    if (name == "assembleRelease" || name == "bundleRelease") {
        doFirst {
            if (!hasReleaseSigningConfig) {
                throw GradleException(
                    "Release signing properties missing. Please provide storeFile, storePassword, keyAlias, and keyPassword in keystore.properties or via environment variables (STORE_FILE, STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)."
                )
            }
        }
    }
}

@Suppress("kotlin:S3416")
dependencies {
    // BOMs
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    // Constraints (API 36 compatibility)
    constraints {
        implementation("androidx.core:core-ktx:1.15.0") { because("API 37 is not targeted yet") }
        implementation("androidx.core:core:1.15.0") { because("API 37 is not targeted yet") }
        implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0") { because("API 37 is not targeted yet") }
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0") {
            because("API 37 is not targeted yet")
        }
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0") { because("API 37 is not targeted yet") }
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0") { because("API 37 is not targeted yet") }
    }

    // Bundles
    implementation(libs.bundles.androidx.core)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.facebook)
    implementation(libs.bundles.firebase)
    implementation(libs.bundles.google)
    implementation(libs.bundles.jackson)
    implementation(libs.bundles.koin)
    testImplementation(libs.org.json)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.room)
    implementation(libs.bundles.ui.logging)

    // Individual Libraries
    implementation(libs.android.youtube.player)
    //noinspection LoginCredentials
    implementation(libs.androidx.credentials)
    //noinspection LoginCredentials
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.glide)
    //noinspection LoginCredentials
    implementation(libs.googleid)
    implementation(libs.guava)

    // KSP & Kapt
    ksp(libs.androidx.room.compiler)

    // Debug
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)

    // Android Test
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.espresso.core)
}

val validateReleaseSecrets = tasks.register("validateReleaseSecrets") {
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
            requiredKeys.addAll(
                listOf(
                    "FACEBOOK_APP_ID",
                    "FACEBOOK_CLIENT_TOKEN",
                    "FB_LOGIN_PROTOCOL_SCHEME"
                )
            )
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
    it.name in listOf("assembleRelease", "bundleRelease")
}.configureEach {
    dependsOn(validateReleaseSecrets)
}
