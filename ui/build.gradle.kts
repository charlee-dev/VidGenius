plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.charleex.autovidyt.ui"
version = "1.0-SNAPSHOT"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.feature.root)
                implementation(projects.feature.router)
                implementation(projects.feature.videoList)
                implementation(projects.feature.videoDetail)
                implementation(projects.feature.dragDrop)
                implementation(compose.desktop.common)
                implementation(compose.desktop.components.splitPane)
                implementation(compose.desktop.components.animatedImage)
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(compose.animationGraphics)
                implementation(compose.animation)
                implementation(libs.jetbrains.compose.ui.util)
                implementation(libs.koin.core)
                implementation(libs.kotlin.dateTime)
                implementation(libs.ballast.core)
                implementation(libs.ballast.navigation)
                implementation(libs.ktor.io.core)
                implementation("com.github.tkuenneth:nativeparameterstoreaccess:0.1.2")
            }
        }
        val jvmMain by getting {
            dependsOn(commonMain)
        }
    }
}