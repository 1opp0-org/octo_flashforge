plugins {

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

//    id("com.android.application") apply false
//    id("com.android.application") version "8.9.2" apply false
//    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
//    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false

    id("org.jetbrains.compose") version "1.6.10" apply false


}

group = "net.amazingdomain.octo_flashforge"
version = "1.0-SNAPSHOT"

