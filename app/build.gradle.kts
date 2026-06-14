import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
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
val hasReleaseSigningConfig = storeFileValue != null &&
        storePasswordValue != null &&
        keyAliasValue != null &&
        keyPasswordValue != null

configure<com.android.build.api.dsl.ApplicationExtension> {
    namespace = "ca.gainzassist"
    //noinspection GradleDependency
    compileSdk = 35

    buildFeatures {
        buildConfig = true
        compose = true
    }

    defaultConfig {
        applicationId = "ca.gainzassist"
        minSdk = 23
        //noinspection OldTargetApi
        targetSdk = 35
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
                throw GradleException("Release signing properties missing. Please provide storeFile, storePassword, keyAlias, and keyPassword in keystore.properties or via environment variables (STORE_FILE, STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD).")
            }
        }
    }
}

@Suppress("kotlin:S3416")
dependencies {
    // BOMs
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(platform(libs.firebase.bom))

    // implementation
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.bundles.androidx.lifecycle)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.bundles.androidx.core)
    implementation(libs.facebook.android.sdk)
    implementation(libs.facebook.rebound)
    implementation(libs.bundles.jackson)
    implementation(libs.bundles.google)
    implementation(libs.bundles.firebase)
    implementation(libs.firebase.crashlytics)
    implementation(libs.parceler.api)
    implementation(libs.guava)
    implementation(libs.bundles.ui.logging)
    implementation(libs.android.youtube.player)
    implementation(libs.glide)

    // kapt
    kapt(libs.androidx.room.compiler)
    kapt(libs.androidx.lifecycle.compiler)
    kapt(libs.parceler)
    kapt(libs.glide.compiler)

    // debugImplementation
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // testImplementation
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.koin.test)

    // androidTestImplementation
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
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
    it.name in listOf("assembleRelease", "bundleRelease")
}.configureEach {
    dependsOn(validateReleaseSecrets)
}
