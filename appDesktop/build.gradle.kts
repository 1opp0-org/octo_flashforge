import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

group = libs.versions.octo.maven.group.get()
version = libs.versions.octo.maven.version.get()


dependencies {
    implementation(project(":crossplatform"))

    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)

    implementation(libs.vlcj) // interfaces binary VLS from desktop
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.simple)  // see configuration on simplelogger.properties
}

compose.desktop {
    application {

        mainClass = "${libs.versions.octo.javaPackage.get()}.desktop.ui.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = libs.versions.octo.javaPackage.get()
            packageVersion = libs.versions.octo.android.versionName.get()
        }
    }
}
