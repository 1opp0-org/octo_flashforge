plugins {

    alias(libs.plugins.kotlin.multiplatform)

    alias(libs.plugins.android.library)

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

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "${libs.versions.octo.android.namespace.get()}.crossplatform"
}

kotlin {
    jvm("desktop") {
    }

    androidTarget {
        publishLibraryVariants("release")
    }


    sourceSets {

        val commonMain by getting // domain models, logic and networking
        val commonTest by getting
        val uiMain by creating // all composables go here

        val androidMain by getting
        val desktopMain by getting

        uiMain.dependsOn(commonMain)

        androidMain.dependsOn(uiMain)
        desktopMain.dependsOn(uiMain)

        commonMain.apply {

            dependencies {

                implementation(libs.kotlinx.coroutines)
                implementation(libs.ktor.network)
                implementation(libs.ktor.io)

                implementation(libs.kotlin.logging)
            }
        }

        commonTest.apply {

            dependencies {
                implementation(libs.junit5.api)
                implementation(libs.junit5.engine.vintage)
                implementation(libs.mockk)
            }
        }

        uiMain.apply {
            dependencies {
                // Jetpack Compose for Desktop dependencies
                implementation(compose.desktop.currentOs)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.runtime)

                implementation(compose.components.resources)

                implementation(compose.material3)       // Material Design 3 components (optional, choose one or use both carefully)
                implementation(compose.materialIconsExtended) // For more Material icons (optional)
                implementation(compose.components.uiToolingPreview)

            }
        }

        androidMain.apply {
            dependencies {
                implementation(compose.preview)
                implementation(compose.components.uiToolingPreview)

                implementation(libs.androidx.compose.ui.tooling)
                implementation(libs.androidx.compose.ui.tooling.preview)
            }


        }

        desktopMain.apply {

            dependencies {
            }
        }
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

// Configure the test task to use JUnit Platform (for JUnit 5)
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}