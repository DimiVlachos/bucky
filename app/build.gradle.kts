import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val architectureTestPattern = "**/architecture/**"
val localProperties =
    Properties().apply {
        rootProject.file("local.properties").takeIf { it.isFile }?.inputStream()?.use { load(it) }
    }

fun signingProperty(name: String): String? =
    providers.gradleProperty(name).orNull ?: localProperties.getProperty(name)

val fossStoreFile = signingProperty("bucky.foss.storeFile")

android {
    namespace = "app.buckykeys.keyboard"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.buckykeys.keyboard"
        minSdk = 26
        targetSdk = 37
        versionCode = 7
        versionName = "0.1.0-alpha7"
    }

    signingConfigs {
        if (fossStoreFile != null) {
            create("fossRelease") {
                storeFile = file(fossStoreFile)
                storePassword =
                    checkNotNull(signingProperty("bucky.foss.storePassword")) {
                        "bucky.foss.storePassword is missing"
                    }
                keyAlias =
                    checkNotNull(signingProperty("bucky.foss.keyAlias")) {
                        "bucky.foss.keyAlias is missing"
                    }
                keyPassword =
                    checkNotNull(signingProperty("bucky.foss.keyPassword")) {
                        "bucky.foss.keyPassword is missing"
                    }
            }
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("play") { dimension = "distribution" }
        create("foss") {
            dimension = "distribution"
            signingConfigs.findByName("fossRelease")?.let { signingConfig = it }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { buildConfig = true }

    testOptions { unitTests.isReturnDefaultValues = true }
}

kotlin { jvmToolchain(17) }

tasks.register<Test>("architectureTest") {
    group = "verification"
    description = "Konsist module-boundary and convention gates."
    val unitTest = tasks.named<Test>("testFossDebugUnitTest").get()
    dependsOn(unitTest.taskDependencies)
    testClassesDirs = unitTest.testClassesDirs
    classpath = unitTest.classpath
    include(architectureTestPattern)
    inputs
        .files(
            rootProject.fileTree(rootProject.projectDir) {
                include("**/src/**/*.kt")
                exclude("**/build/**")
            },
        )
        .withPropertyName("allKotlinSources")
}

dependencies {
    implementation(project(":ime"))
    implementation(project(":settings"))
    "playImplementation"(project(":billing-play"))
    "fossImplementation"(project(":billing-foss"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.timber)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.konsist)
}
