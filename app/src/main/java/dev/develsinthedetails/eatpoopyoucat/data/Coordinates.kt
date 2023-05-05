package dev.develsinthedetails.eatpoopyoucat.data

import java.io.Serializable

data class Coordinates(private var xValue: Float, private var yValue: Float) : Serializable {
    val x: Float
        get() = xValue
    val y: Float
        get() = yValue
}

data class Resolution(val height: Int, val width: Int)
