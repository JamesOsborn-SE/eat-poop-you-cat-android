plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    kotlin("plugin.serialization")
}

apply(plugin = "kotlin-kapt")

android {
    signingConfigs {
        create("release") {
            // You need to specify either an absolute path or include the
            // keystore file in the same directory as the build.gradle file.
            storeFile = file("release-key.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    namespace = "dev.develsinthedetails.eatpoopyoucat"
    compileSdk = 33
    defaultConfig {
        applicationId = "dev.develsinthedetails.eatpoopyoucat"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "0.1"
        testInstrumentationRunner = "dev.develsinthedetails.eatpoopyoucat.utilities.MainTestRunner"
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
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
    buildFeatures {
        compose = true
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    implementation(platform("androidx.compose:compose-bom:2022.10.00"))
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    implementation(platform("androidx.compose:compose-bom:2023.04.01"))
    implementation("androidx.databinding:databinding-adapters:8.0.1")
    implementation("androidx.compose.material:material:1.4.3")

    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    implementation("androidx.activity:activity-compose:1.7.1")
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.runtime:runtime-livedata:1.4.3")
    implementation("androidx.compose.ui:ui-graphics:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.compose.material3:material3:1.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    androidTestImplementation("org.mockito:mockito-android:5.3.1")

    androidTestImplementation(platform("androidx.compose:compose-bom:2023.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(platform("androidx.compose:compose-bom:2022.10.00"))
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    val roomVersion = "2.5.1"
    implementation("androidx.room:room-ktx:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$roomVersion")

    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$roomVersion")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$roomVersion")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$roomVersion")

    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$roomVersion")
    val hiltVersion = "2.45"
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")

    // For Robolectric tests.
    testImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    // ...with Kotlin.
    kaptTest("com.google.dagger:hilt-android-compiler:$hiltVersion")
    // ...with Java.
    testAnnotationProcessor("com.google.dagger:hilt-android-compiler:$hiltVersion")


    // For instrumented tests.
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    // ...with Kotlin.
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:$hiltVersion")
    // ...with Java.
    androidTestAnnotationProcessor("com.google.dagger:hilt-android-compiler:$hiltVersion")


    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}
// Allow references to generated code
kapt {
    correctErrorTypes = true
    useBuildCache = false
}
