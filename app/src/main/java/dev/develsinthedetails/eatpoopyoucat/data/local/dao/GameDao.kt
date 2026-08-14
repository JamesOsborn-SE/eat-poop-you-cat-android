package dev.develsinthedetails.eatpoopyoucat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithRosters
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
interface GameDao {

    @Transaction
    @Query("SELECT * FROM game")
    fun getAll(): Flow<List<Game>>

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    fun get(id: Uuid): Flow<Game>

    @Transaction
    @Query("SELECT * FROM game")
    fun getAllWithEntries(): Flow<List<GameWithEntries>>

    @Transaction
    @Query("SELECT * FROM game")
    suspend fun getAllWithEntriesAsync(): List<GameWithEntries>

    @Transaction
    @Query("SELECT * FROM game")
    suspend fun getAllAsync(): List<Game>

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    fun getWithEntries(id: Uuid): Flow<GameWithEntries>

    @Transaction
    @Query("SELECT * FROM game where id=:id")
    suspend fun getWithEntriesAsync(id: Uuid): GameWithEntries

    @Transaction
    @Query("SELECT * FROM game WHERE turns IS NULL")
    fun getInProgressGamesWithRosters(): Flow<List<GameWithRosters>>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(game: Game)

    @Transaction
    @Query("DELETE FROM Game WHERE id=:id")
    suspend fun delete(id: Uuid)

    @Transaction
    @Query("DELETE FROM Game")
    suspend fun deleteAll()

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Game>)
}
