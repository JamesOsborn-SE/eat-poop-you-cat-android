package dev.develsinthedetails.eatpoopyoucat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Game::class, Player::class, Entry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun entryDao(): EntryDao
    companion object {
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "EatPoopYouCat"
                )
                    .build()
            }
            return INSTANCE as RoomSingleton
        }
    }
}

