plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

apply(plugin = "kotlin-kapt")

android {
    namespace = "dev.develsinthedetails.eatpoopyoucat"
    compileSdk  = 33
    defaultConfig {
        applicationId = "dev.develsinthedetails.eatpoopyoucat"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures{
        compose = true
    }
//    buildToolsVersion = "33.0.2"
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2023.04.01"))
    implementation(platform("androidx.compose:compose-bom:2023.04.01"))
    implementation("androidx.databinding:databinding-adapters:8.0.0")
    implementation("androidx.compose.material:material:1.4.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.04.01"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.04.01"))

    implementation ("androidx.navigation:navigation-compose:2.5.3")
    implementation ("androidx.core:core-ktx:1.10.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation ("androidx.activity:activity-compose:1.7.1")
    implementation(platform("androidx.compose:compose-bom:2023.04.01"))
    implementation ("androidx.compose.ui:ui:1.4.2")
    implementation ("androidx.compose.runtime:runtime-livedata:1.4.2")
    implementation ("androidx.compose.ui:ui-graphics:1.4.2")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.4.2")
    implementation ("androidx.compose.material3:material3:1.1.0-rc01")

    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.04.01"))
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.4.2")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.4.2")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:1.4.2")


    val roomVersion = "2.5.1"

    implementation("androidx.room:room-ktx:$roomVersion")
    annotationProcessor ("androidx.room:room-compiler:$roomVersion")
    kapt ("androidx.room:room-compiler:$roomVersion")

    // optional - RxJava2 support for Room
    implementation ("androidx.room:room-rxjava2:$roomVersion")

    // optional - RxJava3 support for Room
    implementation ("androidx.room:room-rxjava3:$roomVersion")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation ("androidx.room:room-guava:$roomVersion")

    // optional - Test helpers
    testImplementation ("androidx.room:room-testing:$roomVersion")

    // optional - Paging 3 Integration
    implementation( "androidx.room:room-paging:$roomVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation ("com.google.dagger:hilt-android:2.44")
    kapt ("com.google.dagger:hilt-compiler:2.44")
}
// Allow references to generated code
kapt {
  correctErrorTypes = true
  useBuildCache = false
}
