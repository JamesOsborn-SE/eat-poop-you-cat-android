plugins {
    alias(libs.plugins.android.application)
    // REMOVED: alias(libs.plugins.kotlin.android) - No longer needed in AGP 9.0
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "dev.develsinthedetails.eatpoopyoucat"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.develsinthedetails.eatpoopyoucat"
        minSdk = 24
        targetSdk = 36
        versionCode = 24
        versionName = "1.4.7"

        resValue("string", "git_hash", getGitHash())

        testInstrumentationRunner = "dev.develsinthedetails.eatpoopyoucat.utilities.MainTestRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // New AGP 9.0 built-in Kotlin configuration
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

androidComponents {
    onVariants { variant ->
        // Use the new ResValue API
        variant.resValues.put(
            variant.makeResValueKey("string", "applicationId"),
            com.android.build.api.variant.ResValue(variant.applicationId.get())
        )
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.material3)

    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.runtime.livedata)
    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.ui.tooling)
    implementation(libs.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.constraintlayout.compose)
}

fun getGitHash(): String {
    return providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.get().trim()
}