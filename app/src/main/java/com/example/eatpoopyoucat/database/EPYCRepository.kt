package com.example.eatpoopyoucat.database

import kotlinx.coroutines.flow.Flow

class GameRepository(gameDao: GameDao) {
    val allGames: Flow<List<Game>> = gameDao.getAll()
}
