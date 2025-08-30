plugins {

    alias(libs.plugins.kotlin.multiplatform)

}

group = "${libs.versions.octo.maven.group.get()}.core"
version = libs.versions.octo.gradle.version.get()


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.get()))
    }
}

kotlin {
    jvm()

    sourceSets {

        val jvmMain by getting

        val commonMain by getting
        val commonTest by getting

        jvmMain.dependsOn(commonMain)

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
                implementation(libs.mockk)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlin.test)
            }
        }

        jvmMain.apply {
            dependencies {

            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.junit5)
            }
        }
    }
}

// Configure the test task to use JUnit Platform (for JUnit 5)
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
