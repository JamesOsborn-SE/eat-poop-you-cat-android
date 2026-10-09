package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services

import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import io.ktor.http.Url
import kotlin.uuid.Uuid

expect class GameClient(
    serverUrl: Url,
    repository: AppRepository,
    appSettings: AppSettings
) {
    suspend fun getGame(gameId: Uuid): GameWithRosters?
    suspend fun joinGame(player: Roster): Boolean
    suspend fun registerPlayerId(playerId: Uuid): Boolean
    suspend fun turnComplete(newEntry: Entry): Boolean
    suspend fun updateGame(game: GameWithEntries): List<Entry>
    suspend fun ensureGameMatchesServer(gameId: Uuid)
}

