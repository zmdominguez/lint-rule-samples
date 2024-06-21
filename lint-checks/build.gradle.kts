plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.android.lint)
}

lint {
    htmlReport = true
    textReport = true
    ignoreTestSources = true
}

dependencies {

    compileOnly(libs.lint.api)

    compileOnly(libs.lint.checks)
    compileOnly(libs.kotlin.stdlib)

    testImplementation(libs.junit)
    testImplementation(libs.lint.tools)
    testImplementation(libs.lint.tests)
}
