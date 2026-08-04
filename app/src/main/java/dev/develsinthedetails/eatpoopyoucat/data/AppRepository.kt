package dev.develsinthedetails.eatpoopyoucat.data

import dev.develsinthedetails.eatpoopyoucat.data.local.dao.EntryDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.GameDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.PlayerDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.RosterDao
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.data.models.RosterHashAndCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import kotlin.time.Clock
import kotlin.uuid.Uuid

class AppRepository(
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val entryDao: EntryDao,
    private val rosterDao: RosterDao
) {
    // ==========================================
    // Player functions
    // ==========================================
    suspend fun createPlayer(player: Player) {
        playerDao.insert(player.copy(createdAt = Clock.System.now()))
    }

    suspend fun updatePlayer(player: Player) = playerDao.update(player)
    fun getPlayer(id: Uuid): Flow<Player?> = playerDao.get(id)

    // ==========================================
    // Game functions
    // ==========================================
    suspend fun createGame(game: Game) {
        gameDao.insert(game.copy(createdAt = Clock.System.now()))
    }

    suspend fun deleteGame(id: Uuid) = gameDao.delete(id)
    fun getAllGamesWithEntries() = gameDao.getAllWithEntries()
    suspend fun getAllGamesWithEntriesAsync() = gameDao.getAllWithEntriesAsync()
    suspend fun getAllGames() = gameDao.getAllAsync()
    fun getGameWithEntries(id: Uuid) = gameDao.getWithEntries(id)
    suspend fun getGameWithEntriesAsync(id: Uuid) = gameDao.getWithEntriesAsync(id)

    // ==========================================
    // Entry functions
    // ==========================================
    suspend fun createEntry(entry: Entry) =
        entryDao.insert(entry.copy(createdAt = Clock.System.now()))

    fun getEntry(id: Uuid) = entryDao.get(id)
    suspend fun getEntryAsync(id: Uuid) = entryDao.getAsync(id)
    suspend fun updateEntry(entry: Entry) = entryDao.update(entry)
    suspend fun getEntriesAsync(gameId: Uuid) =
        entryDao.getAllEntriesByGameAsync(gameId)

    // ==========================================
    // Roster functions
    // ==========================================
    fun getAllRosters(): Flow<List<Roster>> = rosterDao.getAll()
    fun getRostersByGame(id: Uuid): Flow<List<Roster>> = rosterDao.getAllByGame(id)
    fun getRostersByPlayer(id: Uuid): Flow<List<Roster>> = rosterDao.getAllByPlayer(id)
    suspend fun insert(roster: Roster) = rosterDao.insert(roster)
    suspend fun deleteByGame(gameId: Uuid) = rosterDao.deleteByGame(gameId)
    suspend fun deletePlayer(playerId: Uuid) = rosterDao.deletePlayer(playerId)
    suspend fun delete(gameId: Uuid, playerId: Uuid) = rosterDao.delete(gameId, playerId)
    suspend fun update(roster: Roster) = rosterDao.update(roster)
    suspend fun deleteAll() = rosterDao.deleteAll()
    fun getRosterHashAndCountFlow(gameId: Uuid): Flow<RosterHashAndCount> {
        return rosterDao.getOrderedPlayerIds(gameId).map { playerIds ->
            RosterHashAndCount(
                hash = generateRosterHash(playerIds), count = playerIds.size
            )
        }.distinctUntilChanged()
    }

    private fun generateRosterHash(playerIds: List<Uuid>): String {
        if (playerIds.isEmpty()) return ""
        val combinedIds = playerIds.joinToString(separator = "") { it.toString() }
        val bytes = MessageDigest.getInstance("SHA-256").digest(combinedIds.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}