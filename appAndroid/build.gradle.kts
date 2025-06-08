plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = libs.versions.octo.android.namespace.get()
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = libs.versions.octo.android.namespace.get()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        versionCode = libs.versions.octo.android.versionCode.get().toInt()
        versionName = libs.versions.octo.android.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvm.get()
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":crossplatform"))

    // android libs
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // other
    implementation(libs.ktor.client.core)
    implementation(libs.android.timber)

    ///////////////////////////////////////////////////////////////////////
    // development
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // test
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)

}
