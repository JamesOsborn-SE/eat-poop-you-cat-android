package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppContextProvider
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class StaticRouter {
    fun Route.staticRoutes() {
        get("/{...}") {
            val requestPath = call.request.path().removePrefix("/").ifEmpty { "index.html" }
            val assetPath = "web/$requestPath"

            try {
                val bytes = AppContextProvider.context.assets.open(assetPath).readBytes()

                val contentType = when {
                    requestPath.endsWith(".html") -> ContentType.Text.Html
                    requestPath.endsWith(".js") || requestPath.endsWith(".mjs") -> ContentType(
                        "text",
                        "javascript"
                    )

                    requestPath.endsWith(".wasm") -> ContentType("application", "wasm")
                    requestPath.endsWith(".css") -> ContentType.Text.CSS
                    requestPath.endsWith(".png") -> ContentType.Image.PNG
                    requestPath.endsWith(".svg") -> ContentType.Image.SVG
                    requestPath.endsWith(".ico") -> ContentType.Image.Any
                    requestPath.endsWith(".xml") -> ContentType.Application.Xml
                    requestPath.endsWith(".json") -> ContentType.Application.Json
                    requestPath.endsWith(".cvr") -> ContentType.Application.OctetStream
                    else -> ContentType.Application.OctetStream
                }

                call.respondBytes(bytes, contentType)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}