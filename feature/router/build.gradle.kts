plugins {
    kotlin("multiplatform")
}

group = "com.charleex.autovidyt.feature.root"
version = "1.0-SNAPSHOT"

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.feature.videoList)
                implementation(libs.ballast.core)
                implementation(libs.ballast.savedState)
                implementation(libs.ballast.navigation)
                implementation(libs.koin.core)
            }
        }
    }
}