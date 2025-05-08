plugins {

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.kotlin.jvm) apply false

    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.compose)  apply false
    alias(libs.plugins.kotlin.multiplatform) apply false

}

group = libs.versions.octo.maven.group
version = libs.versions.octo.maven.version

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    dependencies {
        classpath(libs.androidGradle)
        classpath(libs.composeGradle)
        classpath(libs.kotlinGradle)

    }
}