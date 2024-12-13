import java.util.Properties

val properties = Properties()
properties.load(project.rootProject.file("local.properties").reader())

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.rikka.hiddenapi.refine)
    alias(libs.plugins.jetbrains.kotlin.compose)
}

android {
    namespace = "cn.fkj233.deviceemulator"
    compileSdk = 35

    defaultConfig {
        applicationId = "cn.fkj233.deviceemulator"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val key = properties.getProperty("amap.key")
        buildConfigField("String", "AMAP_KEY", "\"$key\"")

        val sign = properties.getProperty("amap.signature")
        if (sign != null && sign.isNotEmpty()) {
            buildConfigField("String", "AMAP_SIGNATURE", "\"$sign\"")
        } else {
            buildConfigField("String", "AMAP_SIGNATURE", "null")
        }

        val packageName = properties.getProperty("amap.fakePackageName")
        if (packageName != null && packageName.isNotEmpty()) {
            buildConfigField("String", "AMAP_FAKE_PACKAGE_NAME", "\"$packageName\"")
        } else {
            buildConfigField("String", "AMAP_FAKE_PACKAGE_NAME", "null")
        }

        manifestPlaceholders["amap"] = key
    }

    signingConfigs {
        create("release") {
            storeFile = file(properties.getProperty("key.file"))
            storePassword = properties.getProperty("key.store.password")
            keyAlias = properties.getProperty("key.alias")
            keyPassword = properties.getProperty("key.password")
        }

        getByName("debug") {
            storeFile = file(properties.getProperty("key.file"))
            storePassword = properties.getProperty("key.store.password")
            keyAlias = properties.getProperty("key.alias")
            keyPassword = properties.getProperty("key.password")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    buildFeatures {
        buildConfig = true
        aidl = true
        compose = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_19.toString()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.compose.accompanist.permissions)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3.window.size)
    implementation("androidx.compose.material:material:1.7.6")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.xposed.ezxhelper)
    compileOnly(libs.xposed.api)

    implementation(project(":XServiceManager"))

    compileOnly(project(":hidden-api"))
    implementation(libs.rikka.hiddenapi.runtime)
    implementation(libs.rikka.hiddenapi.compat)
    compileOnly(libs.rikka.hiddenapi.stub)

//    implementation(libs.amap.map)
//    implementation(libs.amap.search)
    implementation("io.github.TheMelody:gd_compose:1.0.7")

}