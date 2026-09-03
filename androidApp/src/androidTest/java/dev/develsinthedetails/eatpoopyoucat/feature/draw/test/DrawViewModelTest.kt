package dev.develsinthedetails.eatpoopyoucat.feature.draw.test

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawViewModel
import dev.develsinthedetails.eatpoopyoucat.core.utilities.stroke1
import dev.develsinthedetails.eatpoopyoucat.core.utilities.stroke2
import dev.develsinthedetails.eatpoopyoucat.core.utilities.testEntriesGame1
import dev.develsinthedetails.eatpoopyoucat.core.utilities.testPlayerOne
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
    private val mockRepository = mock<dev.develsinthedetails.eatpoopyoucat.data.AppRepository>()
    private lateinit var appSettings: AppSettings

    private val testModule = module {
        single { mockRepository }
        factory { appSettings }
        viewModel { (handle: SavedStateHandle) ->
            DrawViewModel(handle, repository = get(), appSettings = get())
        }
    }

    @get:Rule
    val rule: RuleChain = RuleChain
        .outerRule(KoinTestRule.create {
            modules(testModule)
        })
        .around(instantTaskExecutorRule)

    private val testGameId = testEntriesGame1[0].gameId

    private val viewModel: DrawViewModel by inject {
        parametersOf(
            SavedStateHandle(
                mapOf(
                    "gameId" to testGameId.toString(),
                    "gameMode" to GameMode.LOCAL
                )
            )
        )
    }

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        appSettings = AppSettings(context)

        runBlocking {
            val mSharedPref =
                context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
            mSharedPref!!.edit().putString("PLAYER_ID", testPlayerOne.id.toString())
            appSettings.waitForReady()

            `when`(mockRepository.getLastEntry(testGameId))
                .thenReturn(testEntriesGame1[0])
        }
    }

    @Test
    fun drawViewModel_has_entry_id() = runTest {
        val loadedState = viewModel.uiState.first { !it.isLoading }
        assert(testEntriesGame1[0].id == loadedState.previousEntry?.id)
    }

    fun simulateDrawing(points: List<Offset>, vm: DrawViewModel) {
        if (points.isEmpty()) return

        var previousPoint = points.first()
        vm.touchStart(createPointerInputChange(current = previousPoint, previous = previousPoint, isDown = true, previousDown = false))

        for (i in 1 until points.size) {
            val currentPoint = points[i]
            vm.touchMove(createPointerInputChange(current = currentPoint, previous = previousPoint, isDown = true, previousDown = true))
            previousPoint = currentPoint
        }

        vm.touchUp(createPointerInputChange(current = previousPoint, previous = previousPoint, isDown = false, previousDown = true))
    }

    fun createPointerInputChange(
        current: Offset,
        previous: Offset,
        isDown: Boolean,
        previousDown: Boolean,
        timeMillis: Long = 0L,
        previousTimeMillis: Long = 0L
    ): PointerInputChange {
        return PointerInputChange(
            id = PointerId(0L),
            uptimeMillis = timeMillis,
            position = current,
            pressed = isDown,
            previousUptimeMillis = previousTimeMillis,
            previousPosition = previous,
            previousPressed = previousDown,
            isInitiallyConsumed = false,
            type = PointerType.Touch,
            scrollDelta = Offset.Zero
        )
    }

    @Test
    fun drawing_is_too_simple() = runTest {
        viewModel.setCanvasResolution(1920, 1080)
        simulateDrawing(stroke1, viewModel)
        simulateDrawing(stroke2, viewModel)
        viewModel.isValidDrawing {}

        assert(viewModel.uiState.value.isError)
    }
}