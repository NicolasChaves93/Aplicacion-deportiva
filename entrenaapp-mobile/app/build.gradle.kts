import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// URL base de la API, configurable por maquina sin tocar codigo ni commitear nada.
// Se lee de local.properties (gitignored) como API_BASE_URL=https://xxxx.ngrok-free.app/
// Si no esta definida, cae al valor por defecto para el emulador de Android.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val apiBaseUrl: String = (localProperties.getProperty("API_BASE_URL")
    ?: "http://10.0.2.2:8080/").let { if (it.endsWith("/")) it else "$it/" }

android {
    namespace = "com.entrenaapp.mobile"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.entrenaapp.mobile"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.security.crypto)
    implementation(libs.exifinterface)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}