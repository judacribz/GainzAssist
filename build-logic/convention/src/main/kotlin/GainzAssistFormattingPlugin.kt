import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

@Suppress("unused")
class GainzAssistFormattingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("com.diffplug.spotless")
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                allRules = false
                config.setFrom(files(rootProject.file("config/detekt/detekt.yml")))
                basePath = rootProject.layout.projectDirectory.toString()
            }

            extensions.configure<SpotlessExtension> {
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

            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
            dependencies {
                add("detektPlugins", libs.findLibrary("detekt-compose").get())
            }
        }
    }
}
