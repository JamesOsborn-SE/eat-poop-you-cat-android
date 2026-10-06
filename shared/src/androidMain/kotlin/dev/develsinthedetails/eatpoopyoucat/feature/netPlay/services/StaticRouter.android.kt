@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fromFilePath
import io.ktor.server.request.path
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import korlibs.korlibs_platform.BuildConfig

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class StaticRouter {
    fun Route.staticRoutes() {
        get("/{...}") {
            // compress the files
            // find webApp/build/dist/wasmJs/developmentExecutable/ -type f \( -name "*.wasm" -o -name "*.png" -o -name "*.*js" -o -name "*.png" -o -name "*.css" -o -name "*.html" -o -name "*.svg" -o -name "*.xml" -o -name "*.cvr"  \) -exec gzip -9 -f {} \;
            // find webApp/build/dist/wasmJs/productionExecutable/ -type f \( -name "*.wasm" -o -name "*.png" -o -name "*.*js" -o -name "*.png" -o -name "*.css" -o -name "*.html" -o -name "*.svg" -o -name "*.xml" -o -name "*.cvr"  \) -exec gzip -9 -f {} \;
            // todo CD wipe pngs in ./file for smaller file sizes
            val requestPath = call.request.path().removePrefix("/").ifEmpty { "index.html" }
            val contentType = ContentType.fromFilePath(requestPath).firstOrNull()
                ?: ContentType.Application.OctetStream

            val rootPath = if (BuildConfig.DEBUG) "web-dev" else "web"
            val resourcePath = "$rootPath/${requestPath}.gz"

            val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
                ?: this@StaticRouter.javaClass.classLoader?.getResourceAsStream(resourcePath)

            if (stream != null) {
                val bytes = stream.readBytes()
                val cacheControl = if (requestPath.endsWith(".html")) {
                    "no-cache"
                } else if (requestPath.endsWith(".wasm")) {
                    "public, max-age=31536000" // 1 year .wasm filename changes on build
                } else {
                    "public, max-age=604800" // 7 days; most other filenames do not change on build
                }

                call.response.header(HttpHeaders.CacheControl, cacheControl)
                call.response.header(HttpHeaders.ContentEncoding, "gzip")
                call.respondBytes(bytes, contentType)
            } else {
                println("Error serving static asset: $resourcePath")
                call.respond(HttpStatusCode.NotFound, "Not found")
            }
        }
    }
}