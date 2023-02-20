package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.*
import java.util.*

@Dao
interface EntryDao {
    @Transaction
    @Query("SELECT * FROM entry")
    suspend fun getAll(): List<Entry>

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id")
    suspend fun get(id: UUID): Entry

    @Transaction
    @Query("SELECT * FROM entry WHERE id=:id")
    suspend fun getEntryAndPlayers(id: UUID): Entry

    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: Entry)

    @Transaction
    @Query("DELETE FROM entry")
    suspend fun deleteAll()

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Entry>)
}