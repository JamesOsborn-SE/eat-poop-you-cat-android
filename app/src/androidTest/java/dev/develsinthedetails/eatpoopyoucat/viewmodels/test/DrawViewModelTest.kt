package dev.develsinthedetails.eatpoopyoucat.viewmodels.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import dev.develsinthedetails.eatpoopyoucat.app.SharedPref
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.EntryDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.GameDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.PlayerDao
import dev.develsinthedetails.eatpoopyoucat.data.local.dao.RosterDao
import dev.develsinthedetails.eatpoopyoucat.data.models.Line
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawViewModel
import dev.develsinthedetails.eatpoopyoucat.utilities.getValue
import dev.develsinthedetails.eatpoopyoucat.utilities.testEntriesGame1
import dev.develsinthedetails.eatpoopyoucat.utilities.testPlayerOne
import dev.develsinthedetails.eatpoopyoucat.utilities.testSimpleDrawingJson
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock


class DrawViewModelTest : KoinTest {

    private val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Mocks
    private val mockEntryDao = mock<EntryDao>()
    private val mockGameDao = mock<GameDao>()
    private val mockPlayerDao = mock<PlayerDao>()
    private val mockRosterDao = mock<RosterDao>()

    private val testModule = module {
        single { mockEntryDao }
        single { mockGameDao }
        single { mockPlayerDao }
        single { mockRosterDao }
        single { AppRepository(get(), get(), get(), get()) }
        viewModel { (handle: SavedStateHandle) ->
            DrawViewModel(handle, repository = get())
        }
    }

    @get:Rule
    val rule: RuleChain = RuleChain
        .outerRule(KoinTestRule.create {
            // 2. Start Koin with your test module
            modules(testModule)
        })
        .around(instantTaskExecutorRule)

    private val viewModel: DrawViewModel by inject {
        parametersOf(SavedStateHandle(mapOf("id" to testEntriesGame1[0].id.toString())))
    }

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SharedPref.init(context)
        SharedPref.write(SharedPref.PLAYER_ID, testPlayerOne.id.toString())

        `when`(mockEntryDao.get(testEntriesGame1[0].id))
            .thenReturn(flow {
                emit(testEntriesGame1[0])
            })
    }

    @Test
    fun drawViewModel_has_entry_id() {
        runBlocking {
            assert(testEntriesGame1[0].id == getValue(viewModel.previousEntry).id)
        }
    }

    @Test
    fun drawing_is_too_simple() = runTest {
        val simpleDrawingLines = Json.decodeFromString<List<Line>>(testSimpleDrawingJson)
        viewModel.setCanvasResolution(1920, 1080)

        viewModel.drawingLines.value = simpleDrawingLines

        viewModel.isValidDrawing {}

        assert(viewModel.isError)
    }
}