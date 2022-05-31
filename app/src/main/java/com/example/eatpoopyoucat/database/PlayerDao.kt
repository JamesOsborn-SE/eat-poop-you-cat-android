package com.example.eatpoopyoucat.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

@Dao
interface PlayerDao {

    @Query("SELECT * FROM player")
    fun getAll(): List<Player>

    @Query("SELECT * FROM player WHERE uid=:uid")
    fun get(uid: UUID): Player

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: Player)

    @Query("DELETE FROM player")
    suspend fun deleteAll()
}