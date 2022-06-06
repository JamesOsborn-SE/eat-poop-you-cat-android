package com.example.eatpoopyoucat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
interface EntryDao {
    @Query("SELECT * FROM entry")
    fun getAll(): List<Entry>

    @Query("SELECT * FROM entry WHERE uid=:uid")
    fun get(uid: UUID): Entry

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: Entry)

    @Query("DELETE FROM entry")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<Entry>)
}