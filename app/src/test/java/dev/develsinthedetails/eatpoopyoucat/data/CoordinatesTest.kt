package dev.develsinthedetails.eatpoopyoucat.data

import org.junit.Test


class CoordinatesTest {


    @Test
    fun scale_down() {
        // arrange
        val sourceScreenResolution = ScreenResolution(1000,1000)
        val destinationScreenResolution = ScreenResolution(100,100)

        // act
        val coordinates= Coordinates(100f,100f,sourceScreenResolution,destinationScreenResolution)

        // assert
        assert(coordinates.x == 10f)
    }
    @Test
    fun scale_up() {
        // arrange
        val sourceScreenResolution = ScreenResolution(100,100)
        val destinationScreenResolution = ScreenResolution(1000,1000)

        // act
        val coordinates= Coordinates(10f,10f,sourceScreenResolution,destinationScreenResolution)

        // assert
        assert(coordinates.x == 100f)
    }

}