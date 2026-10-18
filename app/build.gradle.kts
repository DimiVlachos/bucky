plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val architectureTestPattern = "**/architecture/**"

android {
    namespace = "dev.pckeys.keyboard"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.pckeys.keyboard"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("play") { dimension = "distribution" }
        create("foss") { dimension = "distribution" }
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
    inputs.dir(rootProject.layout.projectDirectory.dir("ime/src"))
    inputs.dir(rootProject.layout.projectDirectory.dir("app/src"))
    inputs.dir(rootProject.layout.projectDirectory.dir("settings/src"))
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

    testImplementation(libs.junit)
    testImplementation(libs.konsist)
}
