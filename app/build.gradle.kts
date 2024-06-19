plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
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

    implementation(libs.androidx.core)
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.material)
    implementation(libs.constraintLayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)

    lintChecks(project(":lint-checks"))
}