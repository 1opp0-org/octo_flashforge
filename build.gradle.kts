plugins {

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.compose.multiplatform) apply false

    id("org.jetbrains.compose") version "1.6.10" apply false


}

group = libs.versions.octo.maven.group
version = libs.versions.octo.maven.version

