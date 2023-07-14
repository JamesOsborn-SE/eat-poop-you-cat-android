package dev.develsinthedetails.eatpoopyoucat.data.tests
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dev.develsinthedetails.eatpoopyoucat.data.AppDatabase
import dev.develsinthedetails.eatpoopyoucat.data.EntryDao
import dev.develsinthedetails.eatpoopyoucat.data.GameDao
import dev.develsinthedetails.eatpoopyoucat.data.PlayerDao
import dev.develsinthedetails.eatpoopyoucat.utilities.testEntries
import dev.develsinthedetails.eatpoopyoucat.utilities.testGame
import dev.develsinthedetails.eatpoopyoucat.utilities.testGames
import dev.develsinthedetails.eatpoopyoucat.utilities.testPlayerOne
import dev.develsinthedetails.eatpoopyoucat.utilities.testPlayerTwo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GameDaoTests {
    private lateinit var database: AppDatabase
    private lateinit var gameDao: GameDao
    private lateinit var entryDao: EntryDao
    private lateinit var playerDao: PlayerDao
    private val gameA = testGames[0]
    private val gameB = testGames[1]
    private val gameC = testGames[2]


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        gameDao = database.gameDao()
        entryDao = database.entryDao()
        playerDao = database.playerDao()

        playerDao.insert(testPlayerOne)
        playerDao.insert(testPlayerTwo)
        gameDao.insertAll(listOf(gameA, gameB, gameC))
        // Entries is last because of foreign key constraints
        entryDao.insertAll(testEntries)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testGetAllGames() = runBlocking {
        val gameList = gameDao.getAll().first()
        assertThat(gameList.size, equalTo(3))

        assertThat(gameList[0], equalTo(gameA))
        assertThat(gameList[1], equalTo(gameB))
        assertThat(gameList[2], equalTo(gameC))
    }

    @Test
    fun testGetAllGamesWithEntries() = runBlocking {
        val gameWithEntries = gameDao.getAllWithEntries().first()
        val player= playerDao.get(gameWithEntries[0].entries[0].playerId).first()
        assertThat(testGame, equalTo(gameWithEntries[0].game))
        assert(player?.name == testPlayerOne.name)
    }

    @Test
    fun testDeleteGame() = runBlocking {
        gameDao.delete(gameA.id)
        val gameList = gameDao.getAll().first()
        assertThat(gameList.size, equalTo(2))

        assertThat(gameList[0], equalTo(gameB))
        assertThat(gameList[1], equalTo(gameC))
    }

    @Test
    fun testGetWithEntriesGame() = runBlocking {
        val gameList = gameDao.getWithEntries(gameA.id).first()
        assertThat(gameList.game, equalTo(gameA))
        assertThat(gameList.entries.count(), equalTo(3))
        assertThat(gameList.entries, equalTo(testEntries))
    }


    @Test
    fun testGetInsertGame() = runBlocking {
        gameDao.delete(gameA.id)
        gameDao.insert(gameA)
        val gameList = gameDao.getAll().first()

        assertThat(gameList.size, equalTo(3))

        assertThat(gameList[0], equalTo(gameB))
        assertThat(gameList[1], equalTo(gameC))
        assertThat(gameList[2], equalTo(gameA))
    }
}