plugins {
    `kotlin-dsl`
}

group = "ca.gainzassist.buildlogic"

dependencies {
    implementation(libs.spotless.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("formatting") {
            id = "gainzassist.android.formatting"
            implementationClass = "GainzAssistFormattingPlugin"
        }
    }
}
