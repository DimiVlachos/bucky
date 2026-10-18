plugins { alias(libs.plugins.android.library) }

android {
    namespace = "dev.pckeys.keyboard.billing.play"
    compileSdk = 37

    defaultConfig { minSdk = 26 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions { unitTests.isReturnDefaultValues = true }
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}

dependencies {
    api(project(":billing"))
    testImplementation(libs.junit)
}
