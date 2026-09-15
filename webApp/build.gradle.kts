@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain {
            dependencies {
                implementation(project(":shared"))
            }
        }
    }
}


tasks.register("downloadSqliteWasm") {
    description = "Shitty workaround to get sqlite wasm db working."
    val resourcesDir = file("src/wasmJsMain/resources")
    val version = "3.50.1-build1"

    val mjsFile = File(resourcesDir, "sqlite3.mjs")
    val wasmFile = File(resourcesDir, "sqlite3.wasm")
    val proxyFile = File(resourcesDir, "sqlite3-opfs-async-proxy.js")

    outputs.file(mjsFile)
    outputs.file(wasmFile)
    outputs.file(proxyFile)

    doLast {
        resourcesDir.mkdirs()

        if (!mjsFile.exists()) {
            println("Downloading sqlite3.mjs...")
            URI("https://unpkg.com/@sqlite.org/sqlite-wasm@$version/sqlite-wasm/jswasm/sqlite3.mjs")
                .toURL().openStream().use {
                    Files.copy(it, mjsFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
        }

        if (!wasmFile.exists()) {
            println("Downloading sqlite3.wasm...")
            URI("https://unpkg.com/@sqlite.org/sqlite-wasm@$version/sqlite-wasm/jswasm/sqlite3.wasm")
                .toURL().openStream().use {
                    Files.copy(it, wasmFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
        }
        if (!proxyFile.exists()) {
            println("Downloading sqlite3-opfs-async-proxy.js...")
            URI("https://unpkg.com/@sqlite.org/sqlite-wasm@$version/sqlite-wasm/jswasm/sqlite3-opfs-async-proxy.js")
                .toURL().openStream()
                .use { Files.copy(it, proxyFile.toPath(), StandardCopyOption.REPLACE_EXISTING) }
        }
    }
}

tasks.named("wasmJsProcessResources") {
    dependsOn("downloadSqliteWasm")
}