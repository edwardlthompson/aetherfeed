import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val enableIos: Boolean =
    (findProperty("aetherfeed.enableIos") as String?) == "true" ||
        OperatingSystem.current().isMacOsX

kotlin {
    jvmToolchain(17)
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    if (enableIos) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared-domain"))
                implementation(project(":shared-vault"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
            }
        }
        if (enableIos) {
            val iosMain by creating {
                dependsOn(commonMain)
            }
            val iosX64Main by getting { dependsOn(iosMain) }
            val iosArm64Main by getting { dependsOn(iosMain) }
            val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.aetherfeed.ios.JvmMainKt"
        nativeDistributions {
            targetFormats(TargetFormat.AppImage)
            packageName = "aetherfeed-ios-jvm-stub"
            packageVersion = "1.0.0"
            description = "JVM stub for iOS CMP shell (Windows/Linux dev only)"
        }
    }
}