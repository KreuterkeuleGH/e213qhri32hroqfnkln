plugins {
    // Android Application
    id("com.android.application")

    // Safe Args Plugin registrieren
    id("androidx.navigation.safeargs")
    // oder für Kotlin:
    // id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace        = "com.example.myapplication"
    compileSdk       = 35

    defaultConfig {
        applicationId        = "com.example.myapplication"
        minSdk               = 31
        targetSdk            = 35
        versionCode          = 1
        versionName          = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")

    // Deine bestehenden Dependencies…
    implementation("com.google.dagger:dagger:2.44")
    annotationProcessor("com.google.dagger:dagger-compiler:2.44")
    implementation("javax.inject:javax.inject:1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.preference)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.paging.common.android)
    implementation(libs.work.runtime)
    implementation(libs.work.testing)
    implementation(libs.exoplayer)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
