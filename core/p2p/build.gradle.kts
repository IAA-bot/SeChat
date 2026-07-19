plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.sechat.core.p2p"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:crypto"))
    implementation(project(":core:data"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // WebRTC: add org.webrtc:google-webrtc when available
    // Tor: add info.guardianproject.netcipher:netcipher when available

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
