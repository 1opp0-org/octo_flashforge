pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("jvm").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
        id("org.jetbrains.kotlin.plugin.compose").version(extra["kotlin.version"] as String)

        // android
        id("com.android.library") version "8.9.2"
        id("org.jetbrains.kotlin.android") version "2.0.0"
        id("com.android.application") version "8.9.2"
    }
}


rootProject.name = "octo_flashforge"
//include(":mylibrary")
//include(":octoflashforge")
include(":appAndroid", ":appDesktop")

