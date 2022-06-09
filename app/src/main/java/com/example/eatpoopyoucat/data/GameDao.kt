package com.example.eatpoopyoucat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface GameDao {

    @Query("SELECT * FROM game")
    fun getAll(): Flow<List<Game>>

    @Query("SELECT * FROM game")
    fun getAllWithEntries(): Flow<List<GameWithEntries>>

    @Query("SELECT * FROM game WHERE uid=:uid")
    fun get(uid: UUID): Game

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(game: Game)

    @Query("DELETE FROM Game")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Game>)

}