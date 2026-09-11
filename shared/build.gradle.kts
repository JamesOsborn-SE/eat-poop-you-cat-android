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
val gitHash = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.getOrElse("unknown").trim()
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
    buildConfigField("String", "GIT_HASH", "\"$gitHash\"")
    val isDebug = project.findProperty("isDebug")?.toString()?.toBoolean() ?: true
    buildConfigField("Boolean", "DEBUG", isDebug.toString())
}

compose.resources {
    publicResClass = true
}
compose.desktop {
    application {
        nativeDistributions {
            linux {
                modules("jdk.security.auth")
            }
        }
    }
}
kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    android {
        namespace = "dev.develsinthedetails.eatpoopyoucat"
        androidResources.enable = true
        compileSdk {
            version = release(37)
        }
        minSdk = 26

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
    jvm()

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                api(libs.navigation.compose)
                // Add KMP dependencies here
                api(libs.filekit.core)
                api(libs.filekit.dialogs.compose)
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.lifecycle.viewmodel.savedstate)
                implementation(libs.koin.core)
                api(libs.koin.compose)
                implementation(libs.core.bundle)
                api(libs.koin.compose.viewmodel)
                implementation(libs.kotlin.stdlib)
                api(libs.okio)
                api(libs.compose.components.resources)
                api(libs.datastore.core)
                api(libs.datastore.preferences.core)
                api(libs.androidx.room3.runtime)
                api(libs.androidx.sqlite.bundled)
                api(libs.androidx.compose.ui.unit)
                api(libs.compose.ui)
                api(libs.compose.ui.tooling.preview)
                api(libs.compose.foundation)
                api(libs.material3)

                // Serialization & Ktor
                api(libs.kotlinx.serialization.json)
                api(libs.ktor.client.core)
                api(libs.ktor.client.resources)
                api(libs.ktor.client.content.negotiation)
                api(libs.ktor.client.cio)
                api(libs.ktor.serialization.kotlinx.cbor)

                // Ktor Server
                api(libs.ktor.server.core)
                api(libs.ktor.server.config.yaml)
                api(libs.ktor.server.resources)
                api(libs.ktor.server.netty)
                api(libs.ktor.server.status.pages)
                api(libs.ktor.server.compression)
                api(libs.ktor.server.content.negotiation)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
                api(libs.ktor.client.android)
                implementation(project.dependencies.platform(libs.koin.bom))
                api(libs.datastore.preferences.android)
                api(libs.koin.android)
                api(libs.koin.androidx.compose)
                api(libs.logback.classic)
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
    androidRuntimeClasspath(libs.compose.ui.tooling)
    androidRuntimeClasspath(libs.compose.ui.tooling.preview)
}