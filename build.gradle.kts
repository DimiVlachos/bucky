import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlin {
        target("**/src/**/*.kt")
        targetExclude("**/build/**")
        ktfmt().kotlinlangStyle().configure {
            it.setMaxWidth(100)
            it.setRemoveUnusedImports(true)
        }
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktfmt().kotlinlangStyle()
    }
}

subprojects {
    if (name == "detekt-rules") return@subprojects
    apply(plugin = "io.gitlab.arturbosch.detekt")
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        parallel = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        baseline = rootProject.file("config/detekt/baseline.xml")
        source.setFrom(fileTree("src") { include("*/java/**", "*/kotlin/**") })
    }
    dependencies { "detektPlugins"(project(":detekt-rules")) }
}
