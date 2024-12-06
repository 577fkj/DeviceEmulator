plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.hidden_api"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
    }
}

dependencies {
    annotationProcessor(libs.rikka.hiddenapi.annotation.processor)
    compileOnly(libs.rikka.hiddenapi.annotation)
}