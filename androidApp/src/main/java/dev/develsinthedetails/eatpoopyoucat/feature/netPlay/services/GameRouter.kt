package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import android.content.Context
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.data.models.hash
import dev.develsinthedetails.eatpoopyoucat.data.models.type
import dev.develsinthedetails.eatpoopyoucat.feature.notifications.showTurnNotification
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.Route

fun Route.getAllRoutes(): List<String> {
    val endpoints = mutableListOf<String>()

    fun traverse(route: Route) {
        // If the route node is an HTTP method (GET, POST, etc.), it's a final endpoint
        if (route.selector is HttpMethodRouteSelector) {
            endpoints.add(route.toString())
        }

        // Recursively check all nested routes
        route.children.forEach { traverse(it) }
    }

    traverse(this)
    return endpoints
}

class GameRouter(
    private val repository: AppRepository,
    private val client: Client,
    private val appSettings: AppSettings,
    private val applicationContext: Context,
) {
    fun Route.gameRoutes() {

        get<GameRoot.Id> { gameWithRosters ->
            val gameId = gameWithRosters.id
            val game = repository.getGameWithRosters(gameId)
            if (game != null) {
                call.respond(game)
            } else {
                call.respond(HttpStatusCode.NotFound, "Game not found")
            }
        }

        post<GameRoot.JoinGame> {
            val playerRoster = call.receive<Roster>()
            try {
                repository.addPlayer(playerRoster)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "Could not join game: ${e.message}")
                return@post
            }
            call.respond(HttpStatusCode.OK, "Successfully joined")
        }

        get<GameRoot.Id.AskTakeTurn> { askTakeTurn ->
            val game = repository.getGameWithEntries(askTakeTurn.parent.id)
            val gameRosters = repository.getGameWithRosters(askTakeTurn.parent.id) ?: return@get

            val leader = gameRosters.roster.first()
            var lastEntry = game.entries.maxByOrNull { it.sequence }
            if (appSettings.playerId != leader.playerId) {

                val entries = game.entries.toMutableList()

                // update game entries if needed
                val missing = client.updateGame(leader.address, game)
                missing.forEach {
                    repository.createEntry(it)
                    entries.add(it)
                }
                lastEntry = entries.maxByOrNull { it.sequence }
                // update Roster and Game
                val missingPlayers =
                    client.updateRoster(leader.address, askTakeTurn.parent.id, gameRosters.hash())
                if (missingPlayers != null) {
                    repository.updateGame(missingPlayers.game)
                    missingPlayers.roster.forEach {
                        repository.upsertRoster(it)
                    }
                }
            }
            val destUrl = if (lastEntry == null || lastEntry.type == EntryType.Sentence) {
                "${appSettings.drawDeepLink}/${askTakeTurn.parent.id}/${game.game.gameMode.name}"
            } else {
                "${appSettings.sentenceDeepLink}/${askTakeTurn.parent.id}/${game.game.gameMode.name}"
            }
            showTurnNotification(applicationContext, destUrl)
        }

        put<GameRoot.TakeTurn> {
            val entry = call.receive<Entry>()
            repository.upsertEntry(entry)
        }

        get<Ping> {
            call.respond(HttpStatusCode.OK)
        }

        get<GameRoot.Id.UpdateRoster> { updateRoster ->
            val myHash = repository.getRosterHash(updateRoster.parent.id)
            if (updateRoster.hash != myHash) {
                call.respond(repository.getGameWithRosters(updateRoster.parent.id)!!)
            } else {
                call.respond(HttpStatusCode.OK)
            }
        }

        post<GameRoot.Id.UpdateGame> { updateGame ->
            val knownTurns = call.receive<List<Int>>()
            call.respond(repository.getMissingEntries(updateGame.parent.id, knownTurns))
        }
    }
}