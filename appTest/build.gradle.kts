plugins {

    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":crossplatform"))
    implementation(libs.android.tools.sdk)

    implementation(libs.kotlinx.coroutines)

    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.simple) // this is only needed if you have applications in this module

}
