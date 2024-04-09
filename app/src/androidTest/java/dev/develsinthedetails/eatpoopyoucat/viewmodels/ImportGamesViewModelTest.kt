package dev.develsinthedetails.eatpoopyoucat.viewmodels

import android.util.Log
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import dev.develsinthedetails.eatpoopyoucat.SharedPref
import dev.develsinthedetails.eatpoopyoucat.data.AppDatabase
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.EntryDao
import dev.develsinthedetails.eatpoopyoucat.data.GameDao
import dev.develsinthedetails.eatpoopyoucat.data.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.PlayerDao
import dev.develsinthedetails.eatpoopyoucat.utilities.testEntriesGame1
import dev.develsinthedetails.eatpoopyoucat.utilities.testEntriesGame2
import dev.develsinthedetails.eatpoopyoucat.utilities.testGames
import dev.develsinthedetails.eatpoopyoucat.utilities.testPlayerOne
import dev.develsinthedetails.eatpoopyoucat.utilities.testPlayerTwo
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ImportGamesViewModelTest {
    private val gameA = testGames[0]
    private val gameB = testGames[1]
    private val gameC = testGames[2]

    private lateinit var database: AppDatabase
    private lateinit var repository: AppRepository
    private lateinit var gameDao: GameDao
    private lateinit var entryDao: EntryDao
    private lateinit var playerDao: PlayerDao

    private lateinit var exportedGames: List<GameWithEntries>

    @Before
    fun createDb() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SharedPref.init(context)
        SharedPref.write(SharedPref.PLAYER_ID, testPlayerOne.id.toString())
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        gameDao = database.gameDao()
        entryDao = database.entryDao()
        playerDao = database.playerDao()

        repository = AppRepository(gameDao, playerDao, entryDao)

        playerDao.insert(testPlayerOne)
        playerDao.insert(testPlayerTwo)
        playerDao.insert(testPlayerTwo.copy(id=SharedPref.playerId(), name = "drop tables;"))
        gameDao.insertAll(listOf(gameA, gameC))
        // Entries is last because of foreign key constraints
        entryDao.insertAll(testEntriesGame1)
        exportedGames = gameDao.getAllWithEntriesAsync()
    }

    @After
    fun tearDown() = runBlocking {
        database.entryDao().deleteAll()
        database.gameDao().deleteAll()
        database.playerDao().deleteAll()
    }

    @Test
    fun addGamesWithARemovedPlayer() = runBlocking {
        val games = gameDao.getAllAsync()
        val entries = entryDao.getAllAsync()
        val numberOfGame = games.count()
        val numberOfEntries = entries.count()

        database.playerDao().deleteAll()
        playerDao.insert(testPlayerOne)
        gameDao.delete(games.first().id)
        val uut = ImportGamesViewModel(repository)
        uut.addGames(exportedGames) {}

        val gamesAfterDeleteAndImport = gameDao.getAllAsync()
        val entriesAfterDeleteAndImport = entryDao.getAllAsync()

        val numberOfGameAfterDeleteAndImport = gamesAfterDeleteAndImport.count()
        val numberOfEntriesAfterDeleteAndImport = entriesAfterDeleteAndImport.count()

        assert(numberOfGame == numberOfGameAfterDeleteAndImport)
        assert(numberOfEntries == numberOfEntriesAfterDeleteAndImport)
    }

    @Test
    fun addGamesWithNewEntries() = runBlocking {
        val exportedGamesPlusOne = exportedGames.toMutableList()
        val last = exportedGamesPlusOne.last()
        exportedGamesPlusOne.removeLast()
        var seq = if (last.entries.none()) 0 else last.entries.last().sequence+1
        exportedGamesPlusOne.add(last.copy(
            entries = testEntriesGame2.map {
                Entry(
                    id= UUID.randomUUID(),
                    it.playerId,
                    it.localPlayerName,
                    seq++,
                    gameId = last.game.id,
                    timePassed = 500
                )
            }
        ))
        val games = gameDao.getAllAsync()
        val entries = entryDao.getAllAsync()
        val numberOfGame = games.count()
        val numberOfEntries = entries.count() + testEntriesGame2.count()
        val uut = ImportGamesViewModel(repository)
        val j = async {
            uut.addGames(exportedGamesPlusOne) {}
        }
        j.join()

        val gamesAfterDeleteAndImport = gameDao.getAllAsync()
        val entriesAfterDeleteAndImport = entryDao.getAllAsync()

        val numberOfGameAfterDeleteAndImport = gamesAfterDeleteAndImport.count()
        val numberOfEntriesAfterDeleteAndImport = entriesAfterDeleteAndImport.count()


        assert(numberOfGame == numberOfGameAfterDeleteAndImport)
        assert(numberOfEntries == numberOfEntriesAfterDeleteAndImport)
        Log.i("oof", exportedGamesPlusOne.toString())
        assert(true)
    }

    @Test
    fun addGamesFreshInstall() = runBlocking {
        val games = gameDao.getAllAsync()
        val entries = entryDao.getAllAsync()
        val numberOfGame = games.count()
        val numberOfEntries = entries.count()
        val player = playerDao.getAsync(SharedPref.playerId())

        tearDown()

        playerDao.insert(player!!)
        val uut = ImportGamesViewModel(repository)
        val job = launch {
            uut.addGames(exportedGames) {}
            delay(1000)
        }
        job.join()

        val gamesAfterDeleteAndImport = gameDao.getAllAsync()
        val entriesAfterDeleteAndImport = entryDao.getAllAsync()
        val numberOfGameAfterDeleteAndImport = gamesAfterDeleteAndImport.count()
        val numberOfEntriesAfterDeleteAndImport = entriesAfterDeleteAndImport.count()

        assert(numberOfGame == numberOfGameAfterDeleteAndImport)
        assert(numberOfEntries == numberOfEntriesAfterDeleteAndImport)



    }
}