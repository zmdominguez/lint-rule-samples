object Config {

    object SdkVersions {
        const val compile = 33
        const val min = 23
        const val target = 33
    }

    object AndroidX {
        const val appCompat = "androidx.appcompat:appcompat:1.6.1"
        const val core = "androidx.core:core-ktx:1.10.1"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
        const val material = "com.google.android.material:material:1.9.0"
    }

    private const val kotlinVersion = "1.8.20"
    private const val gradleVersion = "8.1.0"

    object Kotlin {

        const val stdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion"

        // Current preview release: Hedgehog
        //gradlePluginVersion = '8.2.0-alpha07'
        //lintVersion = '31.2.0-alpha07'
    }

    object Plugins {

        const val android = "com.android.tools.build:gradle:8.1.0"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    }

    object Lint {

        // This should always be $gradleVersion major version value + 23
        private const val version = "31.1.0"


        const val api = "com.android.tools.lint:lint-api:$version"
        const val checks = "com.android.tools.lint:lint-checks:$version"
        const val tests = "com.android.tools.lint:lint-tests:$version"
        const val tools = "com.android.tools.lint:lint:$version"
    }

    object Tests {
        const val espressoCore = "androidx.test.espresso:espresso-core:3.5.1"
        const val junit = "junit:junit:4.13.2"
        const val junitExt = "androidx.test.ext:junit:1.1.5"
    }
}