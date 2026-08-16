package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.data.models.RosterHashAndCount
import dev.develsinthedetails.eatpoopyoucat.data.models.type
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.put

class GameRouter(private val repository: AppRepository, private val client: Client, private val appSettings: AppSettings) {
    fun Route.gameRoutes() {
        /**
         * Gets game and roster of player
         */
        get<GetGameWithRosters> { gameWithRosters ->
            val gameId = gameWithRosters.gameId
            val game = repository.getGameWithRosters(gameId)
            if (game != null) {
                call.respond(game)
            } else {
                call.respond(HttpStatusCode.NotFound, "Game not found")
            }
        }
        post<JoinGame> {
            val player = call.receive<Roster>()
            try {
                repository.addPlayer(player)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "Could not join game: ${e.message}")
                return@post
            }
            call.respond(HttpStatusCode.OK, "Successfully joined")
        }
        post<AskTakeTurn> { askTakeTurn ->
            val game = repository.getGameWithEntriesAsync(askTakeTurn.gameId)
            val gameRosters = repository.getGameWithRosters(askTakeTurn.gameId) ?: return@post

            val leaderAddress = gameRosters.roster.first { it.isLeader }.address

            val entries = game.entries.toMutableList()

            // update game entries if needed
            val missing = client.updateGame(leaderAddress, game)
            missing.forEach {
                repository.createEntry(it)
                entries.add(it)
            }

            // update Roster and Game
            val missingPlayers = client.updateRoster(leaderAddress, gameRosters)
            if (missingPlayers != null) {
                repository.updateGame(missingPlayers.game)
                missingPlayers.roster.forEach {
                    repository.updateRoster(it)
                }
            }
            val previousEntry: Entry = entries.maxBy { it.sequence }
            val dest = if (previousEntry.type == EntryType.Sentence)
                "${appSettings.previousGameDetailsDeepLink}/?previousEntryId=${previousEntry.id}"
            else
                "${appSettings.sentenceDeepLink}/?previousEntryId=${previousEntry.id}"

            // TODO Notification

        }
        put<TakeTurn> {
            val entry = call.receive<Entry>()
            repository.createEntry(entry)
        }
        get<Ping> {
            call.respond(HttpStatusCode.OK)
        }
        get<UpdateRoster> { updateRoster ->
            val hash = call.receive<RosterHashAndCount>()
            val myHash = repository.getRosterHashAndCount(updateRoster.gameId)
            if (hash.count != myHash.count || hash.hash != myHash.hash) {
                call.respond(repository.getGameWithRosters(updateRoster.gameId)!!)
            } else {
                call.respond(HttpStatusCode.OK)
            }
        }
        get<UpdateGame> { updateGame ->
            val knownTurns = call.receive<List<Int>>()
            call.respond(repository.getMissingEntriesAsync(updateGame.gameId, knownTurns))
        }
    }
}