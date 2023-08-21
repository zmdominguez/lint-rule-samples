plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = Config.SdkVersions.compile

    defaultConfig {
        minSdk = Config.SdkVersions.min
        targetSdk = Config.SdkVersions.target

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //consumerProguardFiles = "consumer-rules.pro"

        vectorDrawables.useSupportLibrary = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    namespace = "dev.zarah.another_module"
}

dependencies {

    implementation(Config.AndroidX.core)
    implementation(Config.AndroidX.appCompat)
    implementation(Config.AndroidX.material)
    testImplementation(Config.Tests.junit)
    androidTestImplementation(Config.Tests.junitExt)
    androidTestImplementation(Config.Tests.espressoCore)

    lintChecks(project(":lint-checks"))
}