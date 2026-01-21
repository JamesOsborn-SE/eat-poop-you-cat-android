plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.develsinthedetails.eatpoopyoucat"
    compileSdk = 36
    defaultConfig {
        resValue("string", "git_hash", getGitHash())
        applicationId = "dev.develsinthedetails.eatpoopyoucat"
        minSdk = 23
        targetSdk = 36
        versionCode = 24
        versionName = "1.4.7"
        testInstrumentationRunner = "dev.develsinthedetails.eatpoopyoucat.utilities.MainTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        register("release") {
            enableV3Signing = true
            enableV4Signing = true
            if (System.getenv("SIGNING_KEY_STORE_PATH") != null) {
                storeFile = file(System.getenv("SIGNING_KEY_STORE_PATH"))
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.addAll("-Xsuppress-version-warnings", "-Xjvm-default=all")
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    applicationVariants.all {
        resValue("string", "applicationId", applicationId)

        outputs.forEach { output ->
            output as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            if(this.name == "release")
                output.outputFileName = "app-release.apk"
            else
                output.outputFileName = "${applicationId}_${output.versionCode}.apk"
        }
    }
    lint {
        disable.add("MissingTranslation")
    }
}

dependencies {
    implementation(libs.junit.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.constraintlayout.compose)
    implementation(libs.runner)
    androidTestImplementation(libs.core.testing)
    implementation(libs.databinding.adapters)
    implementation(libs.core.splashscreen)
    implementation(libs.core.ktx)
    implementation(libs.navigation.compose)

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.material.icons.extended)
    implementation(libs.ui)
    implementation(libs.runtime.livedata)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material)
    implementation(libs.ui.test)

    implementation(libs.material3)
    implementation(libs.activity.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)


    androidTestImplementation(platform(libs.compose.bom))

    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    implementation(libs.room.ktx)

    // optional - RxJava2 support for Room
    implementation(libs.room.rxjava2)

    // optional - RxJava3 support for Room
    implementation(libs.room.rxjava3)

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation(libs.room.guava)

    // optional - Test helpers
    testImplementation(libs.room.testing)

    // optional - Paging 3 Integration
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    implementation(libs.kotlinx.serialization.json)
}

/**
 * get the git hash
 */
fun getGitHash(): String {
    val gitVersion = providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }
    return gitVersion.standardOutput.asText.get().trim()
}
