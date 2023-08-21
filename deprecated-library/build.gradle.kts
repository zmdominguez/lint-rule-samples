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
//        consumerProguardFiles "consumer-rules.pro"

        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        checkDependencies = true
        baseline = file("lint-baseline.xml")
    }

    namespace = "dev.zarah.deprecated_library"
}

dependencies {

    implementation(project(":another-module"))

    lintChecks(project(":lint-checks"))

    implementation(Config.AndroidX.core)
    implementation(Config.AndroidX.appCompat)
    implementation(Config.AndroidX.material)
    testImplementation(Config.Tests.junit)
    androidTestImplementation(Config.Tests.junitExt)
    androidTestImplementation(Config.Tests.espressoCore)
}