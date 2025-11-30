plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
    id("org.jetbrains.dokka") version "1.9.20"
}

android {
    namespace = "com.example.cobrosmercadoapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cobrosmercadoapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"  // ← Actualizado
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Compose BOM actualizado
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))

    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")

    // Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Google Play Services - Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Maps
    implementation("org.mapsforge:mapsforge-map-android:0.25.0")
    implementation("org.mapsforge:mapsforge-map:0.25.0")
    implementation("org.mapsforge:mapsforge-themes:0.25.0")
    testImplementation(kotlin("test"))

    implementation("at.favre.lib:bcrypt:0.10.2")

    // ===== TEST  =====
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("androidx.test:core-ktx:1.7.0")
}