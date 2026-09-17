package dev.develsinthedetails.eatpoopyoucat.core.utilities

import androidx.compose.ui.geometry.Offset
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * A simple drawing of an uppercase E using two strokes
 */
val stroke1 = listOf(
    Offset(435.97363f, 120.96875f),
    Offset(355.84943f, 120.96875f),
    Offset(325.8429f, 120.96875f),
    Offset(304.82382f, 120.96875f),
    Offset(258.39786f, 122.90805f),
    Offset(195.23845f, 136.98438f),
    Offset(138.1571f, 140.96875f),
    Offset(107.98779f, 150.249f),
    Offset(107.98779f, 155.96875f),
    Offset(107.98779f, 159.95312f),
    Offset(107.98779f, 166.75711f),
    Offset(107.98779f, 173.9961f),
    Offset(107.98779f, 175.96875f),
    Offset(107.98779f, 179.95312f),
    Offset(107.98779f, 186.14066f),
    Offset(107.98779f, 189.95312f),
    Offset(107.98779f, 197.82944f),
    Offset(110.355606f, 220.71017f),
    Offset(139.97998f, 325.4414f),
    Offset(155.52533f, 387.54736f),
    Offset(171.69188f, 457.03705f),
    Offset(174.98242f, 494.9651f),
    Offset(174.98242f, 525.37805f),
    Offset(182.98047f, 546.24005f),
    Offset(184.9748f, 566.95447f),
    Offset(186.97949f, 587.28827f),
    Offset(197.51556f, 624.0197f),
    Offset(198.97656f, 631.45703f),
    Offset(198.97656f, 633.9375f),
    Offset(209.6822f, 626.9453f),
    Offset(252.5823f, 596.3466f),
    Offset(344.91046f, 557.6341f),
    Offset(392.7771f, 535.19824f),
    Offset(419.27277f, 508.00085f),
    Offset(429.36337f, 495.96875f),
    Offset(437.94394f, 493.9864f),
    Offset(445.0013f, 486.93506f),
    Offset(446.16876f, 479.57703f),
    Offset(450.25787f, 471.9453f),
    Offset(453.96924f, 471.9453f),
    Offset(451.96973f, 475.96875f)
)

val stroke2 = listOf(
    Offset(143.979f, 377.96094f),
    Offset(228.17783f, 365.8648f),
    Offset(299.717f, 331.44556f),
    Offset(381.57193f, 288.9439f),
    Offset(441.577f, 252.17862f),
    Offset(468.36365f, 232.16476f),
    Offset(483.4466f, 230.96875f),
    Offset(491.992f, 230.96875f),
    Offset(497.98047f, 230.96875f),
    Offset(495.98096f, 234.95312f)
)

val testGames = arrayListOf(
    Game(
        Uuid.parse("00000000-0000-0000-0000-000000000001"),
        0,
        0
    ),
    Game(
        Uuid.parse("00000000-0000-0000-0000-000000000002"),
        0,
        0
    ),
    Game(
        Uuid.parse("00000000-0000-0000-0000-000000000003"),
        0,
        0
    )
)
val testPlayerOne = Player(
    Uuid.parse("00000000-0000-0000-0000-100000000001"),
    "bob"
)
val testPlayerTwo = Player(
    Uuid.parse("00000000-0000-0000-0000-100000000002"),
    "bobbie"
)


val testDrawing = Gzip.compress(catTestDrawingLinesInJson)
val testGame = testGames[0]
val testEntriesGame1 = arrayListOf(
    Entry(
        id = Uuid.parse("e0000000-0000-0000-0000-000000000001"),
        playerId = testPlayerOne.id,
        sequence = 0,
        sentence = "My cat likes to eat wet food on their birthday",
        drawing = null,
        gameId = testGames[0].id,
        timePassed = 600
    ),
    Entry(
        id = Uuid.parse("e0000000-0000-0000-0000-000000000002"),
        playerId = testPlayerTwo.id,
        sequence = 1,
        sentence = null,
        drawing = testDrawing,
        gameId = testGames[0].id,
        timePassed = 600
    ),
    Entry(
        id = Uuid.parse("e0000000-0000-0000-0000-000000000003"),
        playerId = testPlayerOne.id,
        sequence = 2,
        sentence = "some cats eat hockey pucks",
        drawing = null,
        gameId = testGames[0].id,
        timePassed = 600
    )
)
val testEntriesGame2 = arrayListOf(
    Entry(
        id = Uuid.parse("e0000000-0000-0000-0000-000000000001"),
        playerId = testPlayerOne.id,
        sequence = 0,
        sentence = "My cat likes to eat wet food on their birthday_2",
        drawing = null,
        gameId = testGames[1].id,
        timePassed = 600
    ),
    Entry(
        id = Uuid.parse("e0000000-0000-0000-0000-000000000002"),
        playerId = testPlayerTwo.id,
        sequence = 1,
        sentence = null,
        drawing = testDrawing,
        gameId = testGames[1].id,
        timePassed = 600
    ),
    Entry(
        id = Uuid.parse("e0000000-0000-0000-0000-000000000003"),
        playerId = testPlayerOne.id,
        sequence = 2,
        sentence = "some cats eat hockey pucks_2",
        drawing = null,
        gameId = testGames[1].id,
        timePassed = 600
    )
)
