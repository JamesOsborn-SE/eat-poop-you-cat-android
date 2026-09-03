plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.androidx.room3)
    alias(libs.plugins.android.lint)
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    android {
        namespace = "dev.develsinthedetails.eatpoopyoucat"
        compileSdk {
            version = release(37)
        }
        minSdk = 24

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
                // Add KMP dependencies here
                implementation(libs.kotlin.stdlib)

                api(libs.androidx.datastore.core)
                api(libs.androidx.datastore.preferences.core)
                api(libs.androidx.datastore.preferences)
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
                api(libs.ktor.server.compression.zstd)
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
    // Add any other platform target you use in your project, for example kspDesktop
}