plugins {
    id("java-library")
    id("kotlin")
    id("com.android.lint")
}

lint {
    htmlReport = true
    textReport = true
    ignoreTestSources = true
}

dependencies {

    compileOnly(Config.Lint.api)

    compileOnly(Config.Lint.checks)
    compileOnly(Config.Kotlin.stdLib)

    testImplementation(Config.Tests.junit)
    testImplementation(Config.Lint.tools)
    testImplementation(Config.Lint.tests)
}
