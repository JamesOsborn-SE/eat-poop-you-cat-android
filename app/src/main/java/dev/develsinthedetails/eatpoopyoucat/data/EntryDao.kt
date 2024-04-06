package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface EntryDao {

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id LIMIT 1")
    fun get(id: UUID): Flow<Entry>

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id LIMIT 1")
    suspend fun getAsync(id: UUID): Entry

    @Transaction
    @Query("SELECT * FROM entry WHERE gameId=:id")
    fun getAllEntriesByGame(id: UUID): Flow<List<Entry>>

    @Transaction
    @Query("SELECT * FROM entry WHERE gameId=:gameId")
    suspend fun getAllEntriesByGameAsync(gameId: UUID): List<Entry>

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id")
    suspend fun getEntryAndPlayers(id: UUID): Entry

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: Entry)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertWait(entry: Entry)

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