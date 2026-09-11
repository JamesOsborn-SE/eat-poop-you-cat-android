package dev.develsinthedetails.eatpoopyoucat.feature.importGames

import android.app.Activity
import androidx.room3.Room
import androidx.test.platform.app.InstrumentationRegistry
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.core.utilities.testEntriesGame1
import dev.develsinthedetails.eatpoopyoucat.core.utilities.testEntriesGame2
import dev.develsinthedetails.eatpoopyoucat.core.utilities.testGames
import dev.develsinthedetails.eatpoopyoucat.core.utilities.testPlayerOne
import dev.develsinthedetails.eatpoopyoucat.core.utilities.testPlayerTwo
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.local.AppDatabase
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.EntryDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.GameDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.PlayerDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.RosterDao
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.GameWithEntries
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

class ImportPreviousGamesTest {
    private val gameA = testGames[0]
    private val gameC = testGames[2]

    private lateinit var database: AppDatabase
    private lateinit var repository: AppRepository
    private lateinit var gameDao: GameDao
    private lateinit var entryDao: EntryDao
    private lateinit var playerDao: PlayerDao
    private lateinit var rosterDao: RosterDao

    private val mockAppSettings = mock<AppSettings>()
    private lateinit var exportedGames: List<GameWithEntries>

    @Before
    fun createDb() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val mSharedPref = context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
        mSharedPref!!.edit().putString("PLAYER_ID", testPlayerOne.id.toString())
        `when`(mockAppSettings.playerId).thenReturn(testPlayerOne.id)
        `when`(mockAppSettings.useNicknamesFlow).thenReturn(flowOf(false))

        database = Room.inMemoryDatabaseBuilder<AppDatabase>(
            context,
        ).build()
        gameDao = database.gameDao()
        entryDao = database.entryDao()
        playerDao = database.playerDao()
        rosterDao = database.rosterDao()
        repository = AppRepository(
            gameDao,
            playerDao,
            entryDao,
            rosterDao
        )

        playerDao.insert(testPlayerOne)
        playerDao.insert(testPlayerTwo)
        playerDao.insert(testPlayerTwo.copy(id = testPlayerOne.id, nickname = "drop tables;"))
        gameDao.insertAll(listOf(gameA, gameC))
        // Entries are last because of foreign key constraints
        entryDao.insertAll(testEntriesGame1)
        exportedGames = gameDao.getAllWithEntries()
    }

    @After
    fun tearDown() = runBlocking {
        database.entryDao().deleteAll()
        database.gameDao().deleteAll()
        database.playerDao().deleteAll()
        database.rosterDao().deleteAll()
    }

    @Test
    fun addGamesWithARemovedPlayer() = runBlocking {
        val games = gameDao.getAll()
        val entries = entryDao.getAll()
        val numberOfGame = games.count()
        val numberOfEntries = entries.count()

        database.playerDao().deleteAll()
        playerDao.insert(testPlayerOne)
        gameDao.delete(games.first().id)

        val uut = ImportGamesViewModel(repository, mockAppSettings)
        uut.addGames(exportedGames) {}

        val gamesAfterDeleteAndImport = gameDao.getAll()
        val entriesAfterDeleteAndImport = entryDao.getAll()

        val numberOfGameAfterDeleteAndImport = gamesAfterDeleteAndImport.count()
        val numberOfEntriesAfterDeleteAndImport = entriesAfterDeleteAndImport.count()

        assert(numberOfGame == numberOfGameAfterDeleteAndImport)
        assert(numberOfEntries == numberOfEntriesAfterDeleteAndImport)
    }

    @Test
    fun addGamesWithNewEntries() = runBlocking {
        val exportedGamesPlusOne = exportedGames.toMutableList()
        val last = exportedGamesPlusOne.last()
        exportedGamesPlusOne.removeAt(exportedGamesPlusOne.size - 1)
        var seq = if (last.entries.none()) 0 else last.entries.last().sequence + 1
        exportedGamesPlusOne.add(
            last.copy(
                entries = testEntriesGame2.map {
                    Entry(
                        id = Uuid.random(),
                        it.playerId,
                        it.localPlayerName,
                        seq++,
                        gameId = last.game.id,
                        timePassed = 500
                    )
                }
            ))
        val games = gameDao.getAll()
        val entries = entryDao.getAll()
        val numberOfGame = games.count()
        val numberOfEntries = entries.count() + testEntriesGame2.count()
        val uut = ImportGamesViewModel(repository, mockAppSettings)
        val j = async {
            uut.addGames(exportedGamesPlusOne) {}
        }
        j.join()

        val gamesAfterDeleteAndImport = gameDao.getAll()
        val entriesAfterDeleteAndImport = entryDao.getAll()

        val numberOfGameAfterDeleteAndImport = gamesAfterDeleteAndImport.count()
        val numberOfEntriesAfterDeleteAndImport = entriesAfterDeleteAndImport.count()


        assert(numberOfGame == numberOfGameAfterDeleteAndImport)
        assert(numberOfEntries == numberOfEntriesAfterDeleteAndImport)
    }

    @Test
    fun addGamesFreshInstall() = runBlocking {
        val games = gameDao.getAll()
        val entries = entryDao.getAll()
        val numberOfGame = games.count()
        val numberOfEntries = entries.count()
        tearDown()
        val uut = ImportGamesViewModel(repository, mockAppSettings)
        val job = launch {
            uut.addGames(exportedGames) {}
            delay(1000.milliseconds)
        }
        job.join()

        val gamesAfterDeleteAndImport = gameDao.getAll()
        val entriesAfterDeleteAndImport = entryDao.getAll()
        val numberOfGameAfterDeleteAndImport = gamesAfterDeleteAndImport.count()
        val numberOfEntriesAfterDeleteAndImport = entriesAfterDeleteAndImport.count()

        assert(numberOfGame == numberOfGameAfterDeleteAndImport)
        assert(numberOfEntries == numberOfEntriesAfterDeleteAndImport)

    }
}