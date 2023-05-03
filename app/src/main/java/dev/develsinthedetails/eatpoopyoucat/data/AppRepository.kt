package dev.develsinthedetails.eatpoopyoucat.data

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
    suspend fun createGame(game: Game) = gameDao.insert(game)
    fun getGame(id: UUID) = gameDao.get(id)
    suspend fun createEntry(entry: Entry)=entryDao.insert(entry)
    suspend fun getEntry(id: UUID) = entryDao.get(id)
}
