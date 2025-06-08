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

compose {

    // more info on https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-setup.html#custom-resource-directories
    resources {
        publicResClass = true
        packageOfResClass = "${libs.versions.octo.javaPackage.get()}.crossplatform.ui.resources"
        generateResClass = always
    }
}

dependencies {

    // Jetpack Compose for Desktop dependencies
    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material3)       // Material Design 3 components (optional, choose one or use both carefully)
    implementation(compose.ui)
    implementation(compose.runtime)
    implementation(compose.materialIconsExtended) // For more Material icons (optional)
    implementation(compose.preview) // For @Preview annotations

    implementation(compose.components.resources)

    implementation(libs.kotlinx.coroutines)
    implementation(libs.ktor.network)
    implementation(libs.ktor.io)

    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.simple) // this is only needed if you have applications in this module, which you shouldn't

    ///////////////
    // test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)


}

// Configure the test task to use JUnit Platform (for JUnit 5)
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}