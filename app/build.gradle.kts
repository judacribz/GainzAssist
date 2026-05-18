plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
}

android {
    namespace = "ca.judacribz.gainzassist"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "ca.judacribz.gainzassist"
        minSdk = 21
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
                "META-INF/ASL2.0"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    // 1. Local files
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))

    // 2. Testing
    androidTestImplementation(libs.espresso.core) {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    testImplementation(libs.junit)

    // 3. Architecture Components
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    androidTestImplementation(libs.room.testing)
    implementation(libs.arch.lifecycle.extensions)
    kapt(libs.arch.lifecycle.compiler)

    // 4. Android Support
    implementation(libs.bundles.android.support)

    // 5. Facebook
    implementation(libs.facebook.android.sdk)
    implementation(libs.facebook.rebound)

    // 6. Jackson
    implementation(libs.bundles.jackson)

    // 7. Google
    implementation(libs.bundles.google)

    // 8. Firebase
    implementation(libs.bundles.firebase)

    // 9. Parceler
    implementation(libs.parceler.api)
    kapt(libs.parceler)

    // 10. UI / Logging
    implementation(libs.bundles.ui.logging)
}
