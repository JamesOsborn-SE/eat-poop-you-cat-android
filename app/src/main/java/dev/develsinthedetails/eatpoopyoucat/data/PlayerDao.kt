package dev.develsinthedetails.eatpoopyoucat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface PlayerDao {

    @Query("SELECT * FROM player")
    fun getAll(): Flow<List<Player>>

    @Query("SELECT * FROM player WHERE id=:id")
    fun get(id: UUID): Flow<Player?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: Player)

    @Update
    suspend fun update(player: Player)

    @Query("DELETE FROM player")
    suspend fun deleteAll()
}