package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.Route

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class StaticRouter {
    fun Route.staticRoutes() {
        staticResources("/", "web") {
            default("index.html")
        }
    }
}