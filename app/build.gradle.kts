plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.hydrostop"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.hydrostop"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit para hacer peticiones HTTP
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Gson para convertir JSON a objetos Java
    implementation(libs.gson)

    implementation(libs.okhttp)
}

