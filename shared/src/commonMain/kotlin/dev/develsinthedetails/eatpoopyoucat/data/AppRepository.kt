package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room3.withWriteTransaction
import dev.develsinthedetails.eatpoopyoucat.data.local.AppDatabase
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.EntryDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.GameDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.PlayerDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.RosterDao
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import dev.develsinthedetails.eatpoopyoucat.data.models.NetGame
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.uuid.Uuid

class AppRepository(
    private val db: AppDatabase,
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

    suspend fun upsertPlayer(player: Player) {
        println("upsertPlayer: $player")
        var player = player
        if (player.createdAt == null) {
            player = player.copy(createdAt = Clock.System.now())
        }
        playerDao.upsert(player)
    }

    suspend fun getPlayer(id: Uuid): Player? = playerDao.get(id)

    // ==========================================
    // Game functions
    // ==========================================
    suspend fun createGame(game: Game) {
        gameDao.insert(game.copy(createdAt = Clock.System.now()))
    }

    suspend fun upsertGame(game: Game) = gameDao.upsert(game)
    suspend fun getGame(id: Uuid) = gameDao.get(id)
    suspend fun deleteGame(id: Uuid) = gameDao.delete(id)
    suspend fun getGameWithRosters(gameId: Uuid) = gameDao.getGameWithRosters(gameId)
    suspend fun getAllGames() = gameDao.getAll()
    fun getAllGamesFlow() = gameDao.getAllFlow()
    suspend fun getGameWithEntries(id: Uuid) = gameDao.getWithEntries(id)
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
    suspend fun addPlayer(roster: Roster) {
        playerDao.upsert(Player(roster.playerId, roster.nickname))
        rosterDao.upsert(roster)
    }

    suspend fun delete(gameId: Uuid, playerId: Uuid) = rosterDao.delete(gameId, playerId)
    suspend fun upsertRoster(roster: Roster) = rosterDao.upsert(roster)
    suspend fun upsertRosters(rosters: List<Roster>) = rosterDao.upsert(rosters)
    suspend fun deleteAll() = rosterDao.deleteAll()
    fun getActiveHostedGameWithRostersFlow(playerId: Uuid) =
        gameDao.getActiveHostedGameWithRostersFlow(playerId)

    suspend fun getPlayers(playerIds: List<Uuid>): List<Player> = playerDao.getAll(playerIds)
    suspend fun upsertGameWithRosters(gameWithRosters: GameWithRosters) {
        upsertGame(gameWithRosters.game)
        val existingPlayers = getPlayers(gameWithRosters.roster.map { it.playerId })
        for (playerRoster in gameWithRosters.roster.filterNot { roster -> roster.playerId in existingPlayers.map { it.id } }) {
            upsertPlayer(
                Player(
                    playerRoster.playerId,
                    playerRoster.nickname
                )
            )
        }
        upsertRosters(gameWithRosters.roster)
    }

    suspend fun upsertEntries(entries: List<Entry>) {
        entryDao.upsert(entries)
    }

    fun getNetGameFlow(gameId: Uuid): Flow<NetGame?> = gameDao.getNetGameFlow(gameId)
    suspend fun getGamesForPlayer(playerId: Uuid) = gameDao.getGamesForPlayer(playerId)
    suspend fun updateActiveTurn(gameId: Uuid, activeTurnId: Uuid?) =
        gameDao.updateActiveTurn(gameId, activeTurnId)

    suspend fun getPlayersInGame(gameId: Uuid) = rosterDao.getAllByGame(gameId)
    suspend fun getNetGame(gameId: Uuid) = gameDao.getNetGame(gameId)
    suspend fun upsertNetGame(netGame: NetGame) {
        db.withWriteTransaction {
            gameDao.upsert(netGame.game)
            rosterDao.upsert(netGame.roster)
            entryDao.upsert(netGame.entries)
        }
    }
}