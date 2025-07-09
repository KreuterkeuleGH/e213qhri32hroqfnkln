plugins {
    alias(libs.plugins.android.application)
}

android {
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 31
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

    // Dagger 2
    implementation("com.google.dagger:dagger:2.44")
    annotationProcessor("com.google.dagger:dagger-compiler:2.44")

    implementation("javax.inject:javax.inject:1")


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.preference)
    implementation(libs.paging.common.android)
    implementation(libs.recyclerview)
    implementation(libs.work.runtime)
    implementation(libs.work.testing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation(libs.exoplayer)


    implementation(libs.swiperefreshlayout)
}