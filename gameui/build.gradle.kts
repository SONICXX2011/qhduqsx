plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.gameui"
    compileSdk = 36

    defaultConfig {
        minSdk = 21

        consumerProguardFiles(
            "consumer-rules.pro"
        )
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform(
        "androidx.compose:compose-bom:2025.10.01"
    )

    implementation(composeBom)

    // Activity + Compose
    implementation(
        "androidx.activity:activity-compose:1.10.1"
    )

    // Compose Runtime
    implementation(
        "androidx.compose.runtime:runtime"
    )

    // Compose UI
    implementation(
        "androidx.compose.ui:ui"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )

    // Material 3
    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    // Lifecycle
    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.9.4"
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4"
    )

    // Saved State
    implementation(
        "androidx.savedstate:savedstate-ktx:1.2.1"
    )

    // Debug tooling
    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )
}