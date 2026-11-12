plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

fun readGoldenPathAppVersion(): String {
    val file = rootProject.file("../../schemas/golden-path/app-version.json")
    check(file.isFile) { "Missing Golden Path app version SoT: ${file.invariantSeparatorsPath}" }
    val match = Regex(""""version"\s*:\s*"([^"]+)"""").find(file.readText())
    val version = match?.groupValues?.get(1)?.trim().orEmpty()
    check(version.isNotEmpty()) { "schemas/golden-path/app-version.json missing version" }
    return version
}

android {
    namespace = "org.aetherfeed.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "org.aetherfeed.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = readGoldenPathAppVersion()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    val uploadStoreFile = System.getenv("GOLDENPATH_UPLOAD_STORE_FILE")
    if (!uploadStoreFile.isNullOrBlank()) {
        signingConfigs.create("upload") {
            storeFile = file(uploadStoreFile)
            storePassword = System.getenv("GOLDENPATH_UPLOAD_STORE_PASSWORD").orEmpty()
            keyAlias = System.getenv("GOLDENPATH_UPLOAD_KEY_ALIAS") ?: "upload"
            keyPassword = System.getenv("GOLDENPATH_UPLOAD_KEY_PASSWORD").orEmpty()
        }
        buildTypes.named("release").configure {
            signingConfig = signingConfigs.getByName("upload")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.3.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    testImplementation("androidx.test:core:1.7.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // FOSS ONLY: No proprietary Play Services or closed telemetry SDKs
}
