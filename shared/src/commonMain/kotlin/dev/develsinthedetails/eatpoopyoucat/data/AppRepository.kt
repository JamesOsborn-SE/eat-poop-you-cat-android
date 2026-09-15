package dev.develsinthedetails.eatpoopyoucat.data

import dev.develsinthedetails.eatpoopyoucat.data.local.dao.EntryDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.GameDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.PlayerDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.RosterDao
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import korlibs.crypto.sha256
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.Instant
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

    suspend fun upsertPlayer(player: Player) = playerDao.upsert(player)
    fun getPlayerFlow(id: Uuid): Flow<Player?> = playerDao.getFlow(id)
    suspend fun getPlayer(id: Uuid): Player? = playerDao.get(id)
    suspend fun getAllPlayers(): List<Player> = playerDao.getAll()

    // ==========================================
    // Game functions
    // ==========================================
    suspend fun createGame(game: Game) {
        gameDao.insert(game.copy(createdAt = Clock.System.now()))
    }
    suspend fun upsertGame(game: Game) = gameDao.upsert(game.copy(createdAt = Clock.System.now()))
    fun getGameFlow(id: Uuid) = gameDao.getFlow(id)
    suspend fun getGame(id: Uuid) = gameDao.get(id)
    suspend fun deleteGame(id: Uuid) = gameDao.delete(id)
    fun getAllGamesWithEntriesFlow() = gameDao.getAllWithEntriesFlow()
    fun getInProgressGamesWithRostersFlow(): Flow<List<GameWithRosters>> =
        gameDao.getInProgressGamesWithRostersFlow()

    suspend fun getGameWithRosters(id: Uuid): GameWithRosters? = gameDao.getGameWithRosters(id)
    suspend fun getAllGames() = gameDao.getAll()
    fun getGameWithEntriesFlow(id: Uuid) = gameDao.getWithEntriesFlow(id)
    suspend fun getGameWithEntries(id: Uuid) = gameDao.getWithEntries(id)

    suspend fun updateGame(game: Game) = gameDao.updateGame(game)
    suspend fun getGameIdFromEntry(entryId: Uuid): Uuid = entryDao.getGameId(entryId)

    suspend fun getPreviouslyUsedNicknames(gameId: Uuid): List<String> {
        val nicknames = getGameWithEntries(gameId)
            ?.entries
            ?.mapNotNull { it.localPlayerName?.takeIf { name -> name.isNotBlank() } }
        return nicknames ?: listOf()
    }

    // ==========================================
    // Entry functions
    // ==========================================
    suspend fun createEntry(entry: Entry) =
        entryDao.insert(entry.copy(createdAt = Clock.System.now()))

    suspend fun getEntry(id: Uuid) = entryDao.get(id)
    suspend fun upsertEntry(entry: Entry) =
        entryDao.upsert(entry.copy(createdAt = Clock.System.now()))

    suspend fun getEntries(gameId: Uuid) =
        entryDao.getAllEntriesByGame(gameId)

    suspend fun getMissingEntries(gameId: Uuid, knownTurns: List<Int>) =
        entryDao.getMissingEntries(gameId, knownTurns)

    suspend fun getLastEntry(gameId: Uuid) = entryDao.getLast(gameId)

    // ==========================================
    // Roster functions
    // ==========================================
    suspend fun getAllRosters(): List<Roster> = rosterDao.getAll()
    suspend fun getRostersByGame(id: Uuid): List<Roster> = rosterDao.getAllByGame(id)
    fun getRostersByGameFlow(id: Uuid): Flow<List<Roster>> = rosterDao.getAllByGameFlow(id)
    suspend fun addPlayer(roster: Roster) {
        // todo tor address
        playerDao.upsert(Player(roster.playerId, roster.nickname, lanAddress = roster.address))
        rosterDao.insert(roster)
    }

    suspend fun deleteByGame(gameId: Uuid) = rosterDao.deleteByGame(gameId)
    suspend fun deletePlayer(playerId: Uuid) = rosterDao.deletePlayer(playerId)
    suspend fun delete(gameId: Uuid, playerId: Uuid) = rosterDao.delete(gameId, playerId)
    suspend fun upsertRoster(roster: Roster) = rosterDao.upsert(roster)
    suspend fun upsertRosters(rosters: List<Roster>) = rosterDao.upsert(rosters)
    suspend fun deleteAll() = rosterDao.deleteAll()
    suspend fun updateRosterPing(address: String, gameId: Uuid, time: Instant) =
        rosterDao.updateRosterPing(address, gameId, time)

    suspend fun getRosterHash(gameId: Uuid): String {
        val playerIds = rosterDao.getOrderedPlayerIds(gameId)
        return generateRosterHash(playerIds)
    }

    fun getActiveHostedGameWithRostersFlow(playerId: Uuid) =
        gameDao.getActiveHostedGameWithRostersFlow(playerId)

    companion object {
        fun generateRosterHash(playerIds: List<Uuid>): String {
            if (playerIds.isEmpty()) return ""
            val combinedIds = playerIds.joinToString(separator = "") { it.toString() }
            return combinedIds.encodeToByteArray().sha256().hex
        }
    }
}