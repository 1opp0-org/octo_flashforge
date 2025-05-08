plugins {

    alias(libs.plugins.java.library)

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
    // Import the whole desktop target, includes Skiko, Coroutines, etc.
    implementation(compose.desktop.currentOs) // This brings the core artifacts for the current OS; we could use compose.desktop.common but it doesn't allow for previews

    // Common Compose UI libraries (you'll likely want these)
    implementation(compose.foundation)      // Core Foundation UI elements
    implementation(compose.material)        // Material Design components
    implementation(compose.material3)       // Material Design 3 components (optional, choose one or use both carefully)
    implementation(compose.ui)              // Core UI
    implementation(compose.runtime)         // Core Runtime
    implementation(compose.materialIconsExtended) // For more Material icons (optional)
    implementation(compose.preview) // For @Preview annotations, if you use IntelliJ's Compose preview features


}

// Configure the test task to use JUnit Platform (for JUnit 5)
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}