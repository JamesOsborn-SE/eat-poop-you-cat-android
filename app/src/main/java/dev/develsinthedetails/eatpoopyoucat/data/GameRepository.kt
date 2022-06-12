package dev.develsinthedetails.eatpoopyoucat.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameDao: GameDao
){
    val allGames: Flow<List<Game>> = gameDao.getAll()
    val allGamesWithEntries:Flow<List<GameWithEntries>> = gameDao.getAllWithEntries()

    suspend fun createGame(game: Game) {
        gameDao.insert(game)
    }
}
