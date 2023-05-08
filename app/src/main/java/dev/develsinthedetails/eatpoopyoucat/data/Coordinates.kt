package dev.develsinthedetails.eatpoopyoucat.data


import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(private var xValue: Float, private var yValue: Float) {
    val x: Float
        get() = xValue
    val y: Float
        get() = yValue
}

data class Resolution(val height: Int, val width: Int)
