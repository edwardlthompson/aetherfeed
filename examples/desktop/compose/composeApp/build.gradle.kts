import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":shared-domain"))
    implementation(project(":shared-vault"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "org.aetherfeed.desktop.MainKt"
        nativeDistributions {
            // Win MSI+exe, macOS dmg, Linux deb+AppImage (Compose Desktop / jpackage).
            targetFormats(
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Dmg,
                TargetFormat.Deb,
                TargetFormat.AppImage,
            )
            packageName = "AetherFeed"
            // jpackage requires MAJOR > 0; product marketing version stays 0.1.0 in UI/About.
            packageVersion = "1.0.0"
            description = "Local-first encrypted news, podcast, and booru client"
            vendor = "AetherFeed"
            copyright = "MIT"
            windows {
                menuGroup = "AetherFeed"
                upgradeUuid = "a7c3e8f1-2b4d-4e9a-9c1f-0d8e6a5b4321"
            }
            macOS {
                bundleID = "org.aetherfeed.app"
                // [HUMAN] signing/notarization when identity exists — see PACKAGING.md
            }
            linux {
                packageName = "aetherfeed"
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}