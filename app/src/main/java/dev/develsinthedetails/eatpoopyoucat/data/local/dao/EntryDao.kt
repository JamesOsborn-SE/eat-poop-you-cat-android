package dev.develsinthedetails.eatpoopyoucat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
interface EntryDao {

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id LIMIT 1")
    fun get(id: Uuid): Flow<Entry?>

    @Transaction
    @Query("SELECT * FROM entry")
    suspend fun getAllAsync(): List<Entry?>

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id LIMIT 1")
    suspend fun getAsync(id: Uuid): Entry?

    @Transaction
    @Query("SELECT * FROM entry WHERE gameId=:id")
    fun getAllEntriesByGame(id: Uuid): Flow<List<Entry>>

    @Transaction
    @Query("SELECT * FROM entry WHERE gameId=:gameId")
    suspend fun getAllEntriesByGameAsync(gameId: Uuid): List<Entry>

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id")
    suspend fun getEntryAndPlayersAsync(id: Uuid): Entry

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: Entry)

    @Transaction
    @Query("DELETE FROM entry")
    suspend fun deleteAll()

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Entry>)

    @Transaction
    @Update
    suspend fun update(entry: Entry)
}