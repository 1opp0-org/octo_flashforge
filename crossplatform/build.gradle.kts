plugins {

    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.compose.multiplatform)
    alias(libs.plugins.android.library)

}

group = "${libs.versions.octo.maven.group}.multiplatform"
version = libs.versions.octo.maven.version


//fun composeDependency(groupWithArtifact: String) = "$groupWithArtifact:${libs.versions.kotlin}"

kotlin {
    android(){



    }
    jvmToolchain(17)

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

//    ios()
//    iosSimulatorArm64()
//
//    cocoapods {
//        summary = "Some description for the Shared Module"
//        homepage = "Link to the Shared Module homepage"
//        version = "1.0"
//        ios.deploymentTarget = "14.1"
//        podfile = project.file("../ios/Podfile")
//        framework {
//            baseName = "common"
//            isStatic = true
//        }
//    }


    sourceSets {
        val commonMain by getting {
            dependencies {
//                api(multiplatform.compose.runtime)
//                api(compose.foundation)
//                api(compose.material)
//                api(libs.image.loader)
                implementation(libs.multiplatform.compose.util)


            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))


            }
        }
        val androidMain by getting {
            dependencies {
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }


        val desktopMain by getting {
            dependencies {
//                implementation("uk.co.caprica:vlcj:4.7.0")

            }
        }
        val desktopTest by getting

    }
}


android {
    namespace = libs.versions.octo.android.namespace.get()

    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
