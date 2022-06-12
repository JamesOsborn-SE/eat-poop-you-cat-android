package dev.develsinthedetails.eatpoopyoucat.data

import org.junit.Before
import java.util.*

class GameTest {

    private lateinit var game: Game

    @Before fun setUp() {
        game = Game(UUID.randomUUID(), 0, 0)
    }

}