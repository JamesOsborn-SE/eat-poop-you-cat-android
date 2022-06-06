package com.example.eatpoopyoucat.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.eatpoopyoucat.utilities.entities
import com.example.eatpoopyoucat.utilities.gameWithEntries
import com.example.eatpoopyoucat.utilities.testGame
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.example.eatpoopyoucat.utilities.testGames

@RunWith(AndroidJUnit4::class)
class GameDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var gameDao: GameDao
    private lateinit var entryDao: EntryDao
    private val gameA = testGames[0]
    private val gameB = testGames[1]
    private val gameC = testGames[2]

    @Before
    fun createDb() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        gameDao = database.gameDao()
        entryDao = database.entryDao()

        // Insert plants in non-alphabetical order to test that results are sorted by name
        gameDao.insertAll(listOf(gameA, gameB, gameC))
        entryDao.insertAll(entities)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testGetGames() = runBlocking {
        val gameList = gameDao.getAll().first()
        MatcherAssert.assertThat(gameList.size, Matchers.equalTo(3))

        // Ensure plant list is sorted by name
        MatcherAssert.assertThat(gameList[0], Matchers.equalTo(gameA))
        MatcherAssert.assertThat(gameList[1], Matchers.equalTo(gameB))
        MatcherAssert.assertThat(gameList[2], Matchers.equalTo(gameC))
    }
    @Test
    fun testGetGamesWithEntries() = runBlocking {
        val gameWithEntries = gameDao.getAllWithEntries().first()
        MatcherAssert.assertThat(testGame , Matchers.equalTo(gameWithEntries[0].game))

    }
}