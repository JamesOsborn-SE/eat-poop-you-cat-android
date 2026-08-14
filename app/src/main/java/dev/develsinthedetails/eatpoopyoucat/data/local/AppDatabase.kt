package dev.develsinthedetails.eatpoopyoucat.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.develsinthedetails.eatpoopyoucat.core.utilities.DATABASE_NAME
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.EntryDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.GameDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.PlayerDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.RosterDao
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster

@Database(
    entities = [Game::class, Player::class, Entry::class, Roster::class],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(1,2),
        AutoMigration(2,3),
        AutoMigration(3,4),
    ]
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun entryDao(): EntryDao
    abstract fun rosterDao(): RosterDao

    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .build()
        }
    }
}

