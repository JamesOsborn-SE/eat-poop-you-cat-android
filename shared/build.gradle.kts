@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.androidx.room3)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.buildconfig)
}
fun getGitHash(): String {
    val isCI = providers.environmentVariable("CI").isPresent
    if (!isCI) {
        return "local-dev"
    }
    return providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.get().trim()
}

val scheme = project.findProperty("deeplink.scheme").toString()
val host = project.findProperty("deeplink.host").toString()
val baseUri = "$scheme://$host"

val playUri = "$baseUri${project.findProperty("deeplink.play")}"
val drawUri = "$baseUri${project.findProperty("deeplink.draw")}"
val sentenceUri = "$baseUri${project.findProperty("deeplink.sentence")}"
val previousGamesUri = "$baseUri${project.findProperty("deeplink.previousGames")}"
val previousGameDetailsUri = "$baseUri${project.findProperty("deeplink.previousGameDetails")}"

buildConfig {
    packageName("dev.develsinthedetails.eatpoopyoucat.config")

    useKotlinOutput {
        topLevelConstants = true
        internalVisibility = false
    }
    buildConfigField("String", "DEEPLINK_SCHEME", "\"$scheme\"")
    buildConfigField("String", "DEEPLINK_HOST", "\"$host\"")
    buildConfigField("String", "DEEPLINK_PLAY", "\"${project.findProperty("deeplink.play")}\"")
    buildConfigField("String", "DEEPLINK_DRAW", "\"${project.findProperty("deeplink.draw")}\"")
    buildConfigField(
        "String",
        "DEEPLINK_SENTENCE",
        "\"${project.findProperty("deeplink.sentence")}\""
    )
    buildConfigField(
        "String",
        "DEEPLINK_PREVIOUS_GAMES",
        "\"${project.findProperty("deeplink.previousGames")}\""
    )
    buildConfigField(
        "String",
        "DEEPLINK_PREVIOUS_GAME_DETAILS",
        "\"${project.findProperty("deeplink.previousGameDetails")}\""
    )

    buildConfigField("String", "DEEPLINK_BASE_URI", "\"$baseUri\"")
    buildConfigField("String", "DEEPLINK_PLAY_URI", "\"$playUri\"")
    buildConfigField("String", "DEEPLINK_DRAW_URI", "\"$drawUri\"")
    buildConfigField("String", "DEEPLINK_SENTENCE_URI", "\"$sentenceUri\"")
    buildConfigField("String", "DEEPLINK_PREVIOUS_GAMES_URI", "\"$previousGamesUri\"")
    buildConfigField("String", "DEEPLINK_PREVIOUS_GAME_DETAILS_URI", "\"$previousGameDetailsUri\"")

    buildConfigField("String", "VERSION_NAME", "\"${project.version}\"")
    buildConfigField("String", "GIT_HASH", "\"${getGitHash()}\"")
    val isDebug = project.findProperty("isDebug")?.toString()?.toBoolean() ?: true
    buildConfigField("Boolean", "DEBUG", isDebug.toString())
}

compose.resources {
    publicResClass = true
}

fun KotlinDependencyHandler.jvmAndAndroidDependencies() {
    api(libs.androidx.sqlite.bundled)
    implementation(libs.androidx.compose.ui.unit)
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.content.negotiation)
    // Ktor Client
    implementation(libs.ktor.client.cio)
}

kotlin {
    jvmToolchain(17)
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

    android {
        namespace = "dev.develsinthedetails.eatpoopyoucat"
        androidResources.enable = true
        compileSdk {
            version = release(37)
        }
        minSdk = 26

        withHostTestBuilder { }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm()

    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.korlibs.compression)
                api(libs.korlibs.crypto)
                api(libs.kotlinx.datetime)
                api(libs.navigation.compose)
                api(libs.filekit.core)
                api(libs.filekit.dialogs.compose)
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.lifecycle.viewmodel.savedstate)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                api(libs.compose.ui.graphics)
                api(libs.koin.compose)
                implementation(libs.core.bundle)
                api(libs.koin.compose.viewmodel)
                implementation(libs.kotlin.stdlib)
                api(libs.okio)
                api(libs.compose.components.resources)
                api(libs.datastore.core)
                api(libs.datastore.preferences.core)
                api(libs.androidx.room3.runtime)

                api(libs.compose.ui)
                api(libs.compose.ui.tooling.preview)
                api(libs.compose.foundation)
                api(libs.material3)

                // Serialization & Ktor
                api(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                api(libs.ktor.client.resources)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.serialization.kotlinx.cbor)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                jvmAndAndroidDependencies()

                api(libs.ktor.client.android)
                implementation(project.dependencies.platform(libs.koin.bom))
                api(libs.datastore.preferences.android)
                api(libs.koin.android)
                api(libs.koin.androidx.compose)
                api(libs.logback.classic)
            }
        }

        jvmMain {
            dependencies {
                jvmAndAndroidDependencies()
            }
        }

        wasmJsMain {
            languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop")
            dependencies {
                implementation(libs.kotlinXw3c)
                implementation(libs.androidx.sqlite.web)
                implementation(libs.ktor.client.js)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                api(libs.androidx.core)
                api(libs.ext.junit)
                api(libs.runner)
            }
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspCommonMainMetadata", libs.androidx.room3.compiler)
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspJvm", libs.androidx.room3.compiler)
    add("kspWasmJs", libs.androidx.room3.compiler)
    androidRuntimeClasspath(libs.compose.ui.tooling)
    androidRuntimeClasspath(libs.compose.ui.tooling.preview)
}