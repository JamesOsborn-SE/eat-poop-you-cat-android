plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization")
    id("kotlin-kapt") apply true
}

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
    compileSdk = 34
    defaultConfig {
        applicationId = "dev.develsinthedetails.eatpoopyoucat"
        minSdk = 21
        targetSdk = 34
        versionCode = 12
        versionName = "1.1.2"
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
}

dependencies {
    val compseBom = "2024.03.00"
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    implementation(platform("androidx.compose:compose-bom:$compseBom"))
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    implementation("androidx.databinding:databinding-adapters:8.3.1")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    val lifecycleVersion ="2.7.0"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    val composeVersion = "1.6.4"
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("androidx.compose.ui:ui-graphics:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui-test:$composeVersion")

    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.activity:activity-compose:1.8.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    androidTestImplementation("org.mockito:mockito-android:5.5.0")

    androidTestImplementation(platform("androidx.compose:compose-bom:$compseBom"))

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-ktx:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

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
    val hiltVersion = "2.50"
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
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


    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    //noinspection KaptUsageInsteadOfKsp not supported by Hilt yet https://dagger.dev/dev-guide/ksp.html
    kapt("androidx.room:room-compiler:$roomVersion")
}
// Allow references to generated code
kapt {
    correctErrorTypes = true
    useBuildCache = false
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}
