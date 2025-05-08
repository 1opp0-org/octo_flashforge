plugins {

    alias(libs.plugins.kotlin.jvm)

    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

group = "${libs.versions.octo.maven.group.get()}.crossplatform"
version = libs.versions.octo.gradle.version.get()


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.get()))
    }
}

dependencies {

    // Jetpack Compose for Desktop dependencies
    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)      // Core Foundation UI elements
    implementation(compose.material3)       // Material Design 3 components (optional, choose one or use both carefully)
    implementation(compose.ui)              // Core UI
    implementation(compose.runtime)         // Core Runtime
    implementation(compose.materialIconsExtended) // For more Material icons (optional)
    implementation(compose.preview) // For @Preview annotations, if you use IntelliJ's Compose preview features

    implementation(libs.kotlinx.coroutines)
    implementation(libs.ktor.network)
    implementation(libs.ktor.io)


}

// Configure the test task to use JUnit Platform (for JUnit 5)
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}