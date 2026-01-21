plugins {
    alias(libs.plugins.android.application)
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

    //noinspection WrongGradleMethod
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

    testImplementation(platform(libs.koin.bom))
    testImplementation(libs.koin.test)
    testImplementation(libs.junit.ktx)
    testImplementation(libs.core.testing)
    testImplementation(libs.koin.test.junit4)

    androidTestImplementation(platform(libs.koin.bom))
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.koin.android.test)
    androidTestImplementation(libs.koin.test.junit4)
    androidTestImplementation(libs.core.testing)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.ui.test)
    androidTestImplementation(libs.ui.test.junit4)
    androidTestImplementation(libs.mockito.kotlin)

    testImplementation(libs.ext.junit)
}

fun getGitHash(): String {
    return providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.get().trim()
}