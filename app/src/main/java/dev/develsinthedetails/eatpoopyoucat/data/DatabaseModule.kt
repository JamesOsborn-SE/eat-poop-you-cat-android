package dev.develsinthedetails.eatpoopyoucat.data

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
//Repositories will live same as the activity that requires them
@InstallIn(ActivityComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun providesAppRepository(impl: AppRepositoryImpl): IAppRepository

}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(appContext,
            AppDatabase::class.java,
            "EatPoopYouCat"
        ).build()
    }

    @Provides
    fun provideGameDao(appDatabase: AppDatabase): GameDao {
        return appDatabase.gameDao()
    }

    @Provides
    fun provideEntryDao(appDatabase: AppDatabase): EntryDao {
        return appDatabase.entryDao()
    }

    @Provides
    fun providePlayerDao(appDatabase: AppDatabase): PlayerDao {
        return appDatabase.playerDao()
    }
}