plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.screenshot)
}

android {
    namespace = "dev.develsinthedetails.eatpoopyoucat"
    compileSdk = 37
    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    defaultConfig {
        applicationId = "dev.develsinthedetails.eatpoopyoucat"
        minSdk = 26
        targetSdk = 37
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

    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
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
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(platform(libs.compose.bom))
    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.androidx.ui.tooling)
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
    implementation(libs.material.icons.extended)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.constraintlayout.compose)

    implementation(libs.ktor.serialization.kotlinx.cbor)
    implementation(libs.ktor.client.resources)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.compression.zstd)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.logback.classic)

    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    testImplementation(libs.koin.test)
    testImplementation(libs.junit.ktx)

    androidTestImplementation(libs.core.testing)

    androidTestImplementation(libs.koin.test.junit4)
    androidTestImplementation(libs.koin.android.test)

    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.ext.junit)

    androidTestImplementation(libs.ui.test)
    androidTestImplementation(libs.ui.test.junit4)

    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
}

fun getGitHash(): String {
    return providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.get().trim()
}
