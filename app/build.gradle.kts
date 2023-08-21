plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    compileSdk = Config.SdkVersions.compile

    defaultConfig {
        applicationId = "dev.zarah.sdksample"
        minSdk = Config.SdkVersions.min
        targetSdk = Config.SdkVersions.target
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    namespace = "dev.zarah.sdksample"

    lint {
        checkDependencies = true
        textReport = true
        baseline = file("lint-baseline.xml")
    }
}

dependencies {

    implementation(project(":deprecated-library"))

    implementation(Config.AndroidX.core)
    implementation(Config.AndroidX.appCompat)
    implementation(Config.AndroidX.material)
    implementation(Config.AndroidX.constraintLayout)
    testImplementation(Config.Tests.junit)
    androidTestImplementation(Config.Tests.junitExt)
    androidTestImplementation(Config.Tests.espressoCore)

    lintChecks(project(":lint-checks"))
}