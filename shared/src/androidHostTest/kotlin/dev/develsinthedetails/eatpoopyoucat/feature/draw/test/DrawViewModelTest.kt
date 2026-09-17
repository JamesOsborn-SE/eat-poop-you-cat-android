package dev.develsinthedetails.eatpoopyoucat.feature.draw.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.lifecycle.SavedStateHandle
import dev.develsinthedetails.eatpoopyoucat.app.AppSettings
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.MainDispatcherRule
import dev.develsinthedetails.eatpoopyoucat.core.utilities.stroke1
import dev.develsinthedetails.eatpoopyoucat.core.utilities.stroke2
import dev.develsinthedetails.eatpoopyoucat.core.utilities.testEntriesGame1
import dev.develsinthedetails.eatpoopyoucat.core.utilities.testPlayerOne
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.feature.draw.DrawViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DrawViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository = mock<AppRepository>()
    private val mockAppSettings = mock<AppSettings>()
    private lateinit var viewModel: DrawViewModel
    private val testGameId = testEntriesGame1[0].gameId

    @Before
    fun setUp() = runBlocking {
        `when`(mockAppSettings.playerId).thenReturn(testPlayerOne.id)
        `when`(mockAppSettings.useNicknamesFlow).thenReturn(flowOf(false))

        `when`(mockRepository.getLastEntry(testGameId)).thenReturn(testEntriesGame1[0])

        val savedStateHandle = SavedStateHandle(
            mapOf(
                "gameId" to testGameId.toString(),
                "gameMode" to GameMode.LOCAL.toString()
            )
        )
        viewModel = DrawViewModel(
            state = savedStateHandle,
            repository = mockRepository,
            appSettings = mockAppSettings
        )
    }

    @Test
    fun drawViewModel_has_entry_id() = runTest {
        val loadedState = viewModel.uiState.first { !it.isLoading }
        assert(testEntriesGame1[0].id == loadedState.previousEntry?.id)
    }

    @Test
    fun drawing_is_too_simple() = runTest {
        viewModel.setCanvasResolution(1920, 1080)
        simulateDrawing(stroke1, viewModel)
        simulateDrawing(stroke2, viewModel)

        viewModel.isValidDrawing {}

        assert(viewModel.uiState.value.isError)
    }

    fun simulateDrawing(points: List<Offset>, vm: DrawViewModel) {
        if (points.isEmpty()) return

        var previousPoint = points.first()
        vm.touchStart(
            createPointerInputChange(
                current = previousPoint,
                previous = previousPoint,
                isDown = true,
                previousDown = false
            )
        )

        for (i in 1 until points.size) {
            val currentPoint = points[i]
            vm.touchMove(
                createPointerInputChange(
                    current = currentPoint,
                    previous = previousPoint,
                    isDown = true,
                    previousDown = true
                )
            )
            previousPoint = currentPoint
        }

        vm.touchUp(
            createPointerInputChange(
                current = previousPoint,
                previous = previousPoint,
                isDown = false,
                previousDown = true
            )
        )
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

}