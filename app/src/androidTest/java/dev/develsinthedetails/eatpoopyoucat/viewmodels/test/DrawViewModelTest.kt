package dev.develsinthedetails.eatpoopyoucat.viewmodels.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.develsinthedetails.eatpoopyoucat.SharedPref
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.EntryDao
import dev.develsinthedetails.eatpoopyoucat.data.GameDao
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.PlayerDao
import dev.develsinthedetails.eatpoopyoucat.utilities.getValue
import dev.develsinthedetails.eatpoopyoucat.utilities.testEntries
import dev.develsinthedetails.eatpoopyoucat.utilities.testPlayerOne
import dev.develsinthedetails.eatpoopyoucat.utilities.testSimpleDrawingJson
import dev.develsinthedetails.eatpoopyoucat.viewmodels.DrawViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import java.util.UUID
import javax.inject.Inject


@HiltAndroidTest
class DrawViewModelTest {
    private lateinit var viewModel: DrawViewModel
    private val hiltRule = HiltAndroidRule(this)
    private val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val rule: RuleChain = RuleChain
        .outerRule(hiltRule)
        .around(instantTaskExecutorRule)

    @set:Inject
    private lateinit var appRepository: AppRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        SharedPref.init(context)
        SharedPref.write(SharedPref.PLAYER_ID, testPlayerOne.id.toString())
        val mockEntryDao = mock<EntryDao>()
        `when`(mockEntryDao.get(UUID.fromString(testEntries[0].id.toString())))
            .thenReturn(flow {
            emit(testEntries[0])
        })
        val mockGameDao = mock<GameDao>()
        val mockPlayerDao = mock<PlayerDao>()
        appRepository = AppRepository(
            gameDao = mockGameDao,
            entryDao = mockEntryDao,
            playerDao = mockPlayerDao
        )

        val savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
            set("EntryId", testEntries[0].id.toString())
        }
        viewModel = DrawViewModel(savedStateHandle, appRepository)
    }


    @Test
    fun drawViewModel_has_entry_id() {
        runBlocking {
            assert(testEntries[0].id == getValue(viewModel.previousEntry).id)
        }
    }
    @Test
    fun drawing_is_too_simple() = runTest{
        val intSharedFlow = MutableStateFlow(listOf<Line>())

        val simpleDrawingLines = Json.decodeFromString<List<Line>>(testSimpleDrawingJson)
        viewModel.setCanvasResolution(1920, 1080)
        intSharedFlow.value = simpleDrawingLines
        val field = DrawViewModel::class.java.getDeclaredField("drawingLines")
        field.isAccessible = true
        field.set(viewModel, intSharedFlow)

        viewModel.isValidDrawing {}

        assert(viewModel.isError)
    }
}