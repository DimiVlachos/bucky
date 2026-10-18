plugins { alias(libs.plugins.kotlin.jvm) }

kotlin { jvmToolchain(17) }

dependencies {
    compileOnly(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.junit)
}

val detektBaseline = rootProject.file("config/detekt/baseline.xml")
val detektConfig = rootProject.file("config/detekt/detekt.yml")

tasks.test {
    inputs.file(detektBaseline)
    inputs.file(detektConfig)
    systemProperty("pckeys.detekt.baseline", detektBaseline.absolutePath)
    systemProperty("pckeys.detekt.config", detektConfig.absolutePath)
    systemProperty("pckeys.detekt.testDir", project.file("src/test/kotlin").absolutePath)
}
