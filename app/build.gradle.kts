plugins { alias(libs.plugins.android.application) }

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

dependencies {
    implementation(project(":ime"))
    implementation(project(":settings"))
    "playImplementation"(project(":billing-play"))
    "fossImplementation"(project(":billing-foss"))

    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
}
