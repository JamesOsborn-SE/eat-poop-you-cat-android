package dev.develsinthedetails.eatpoopyoucat.utilities

import androidx.lifecycle.LiveData
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.Game
import dev.develsinthedetails.eatpoopyoucat.data.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.data.Player
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

val testGames = arrayListOf(
    Game(UUID.fromString("00000000-0000-0000-0000-000000000001"), 0, 0),
    Game(UUID.fromString("00000000-0000-0000-0000-000000000002"), 0, 0),
    Game(UUID.fromString("00000000-0000-0000-0000-000000000003"), 0, 0)
)
val testPlayerOne = Player(UUID.fromString("00000000-0000-0000-0000-100000000001"), "bob")
val testPlayerTwo = Player(UUID.fromString("00000000-0000-0000-0000-100000000002"), "bobbie")


val testDrawing = Gzip.compress(catTestDrawingLinesInJson)
val testGame = testGames[0]
val testEntries = arrayListOf(
    Entry(
        id = UUID.fromString("e0000000-0000-0000-0000-000000000001"),
        playerId = testPlayerOne.id,
        sequence = 0,
        sentence = "My cat likes to eat wet food on their birthday",
        drawing =  null,
        gameId = testGames[0].id,
        timePassed = 600
    ),
    Entry(
        id = UUID.fromString("e0000000-0000-0000-0000-000000000002"),
        playerId = testPlayerTwo.id,
        sequence = 1,
        sentence = null,
        drawing = testDrawing,
        gameId = testGames[0].id,
        timePassed = 600
    ),
    Entry(
        id = UUID.fromString("e0000000-0000-0000-0000-000000000003"),
        playerId = testPlayerOne.id,
        sequence = 2,
        sentence = "some cats eat hockey pucks",
        drawing =  null,
        gameId = testGames[0].id,
        timePassed = 600
    )
)

val gameWithEntries = GameWithEntries(testGame, testEntries)

@Throws(InterruptedException::class)
fun <T> getValue(liveData: LiveData<T>): T {
    val data = arrayOfNulls<Any>(1)
    val latch = CountDownLatch(1)
    liveData.observeForever { o ->
        data[0] = o
        latch.countDown()
    }
    latch.await(2, TimeUnit.SECONDS)

    @Suppress("UNCHECKED_CAST")
    return data[0] as T
}