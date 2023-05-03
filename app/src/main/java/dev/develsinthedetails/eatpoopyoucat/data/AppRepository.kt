package dev.develsinthedetails.eatpoopyoucat.data

import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

//interface IAppRepository {
//    val allGames: Flow<List<Game>>
//    val allGamesWithEntries: Flow<List<GameWithEntries>>
//    val allPlayers: Flow<List<Player>>
//
//    suspend fun createPlayer(player: Player)
//    suspend fun createGame(game: Game)
//    suspend fun getGame(id: UUID): Game
//    suspend fun createEntry(entry: Entry)
//    suspend fun getEntry(id: UUID)
//}

@Singleton
class AppRepository @Inject constructor(
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val entryDao: EntryDao
) {
    suspend fun createPlayer(player: Player) = playerDao.insert(player)
    suspend fun updatePlayer(player: Player) = playerDao.update(player)
    fun getPlayer(id: UUID): Flow<Player?> = playerDao.get(id)
    fun getPlayer(id: String): Flow<Player?> = playerDao.get(UUID.fromString(id))
    suspend fun createGame(game: Game) = gameDao.insert(game)
    fun createG(game: Game) = gameDao.insertWait(game)
    fun getGame(id: UUID) = gameDao.get(id)
    fun getGame(id: String) = gameDao.get(UUID.fromString(id))
    suspend fun createEntry(entry: Entry) = entryDao.insert(entry)
    fun getEntry(id: UUID): Flow<Entry> = entryDao.get(id)
    fun getEntry(id: String) = entryDao.get(UUID.fromString(id))
    suspend fun getAllEntries(): List<Entry> = entryDao.getAll()
}
