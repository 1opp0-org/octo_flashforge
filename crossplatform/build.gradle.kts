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

    androidTarget(){
        publishLibraryVariants("release")
    }


    sourceSets {

        val jvmMain by creating

        val commonMain by getting
        val commonTest by getting

        val appTest by creating // applications made for testing the library

        val androidMain by getting
        val desktopMain by getting

        jvmMain.dependsOn(commonMain)

        androidMain.dependsOn(jvmMain)
        desktopMain.dependsOn(jvmMain)

        commonMain.apply {

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

            }
        }

        commonTest.apply {

            dependencies {
                implementation(libs.junit)
                implementation(libs.mockk)
            }
        }

        jvmMain.apply {
            dependencies {
                implementation(compose.components.uiToolingPreview)
            }
        }

        appTest.apply {
            dependencies {
                implementation(libs.slf4j.simple) // this is only needed if you have applications in this module, which you shouldn't
            }
        }

        androidMain.apply {
            dependencies {
                implementation(compose.preview)
                implementation(compose.components.uiToolingPreview)

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