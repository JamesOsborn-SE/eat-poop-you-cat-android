package dev.develsinthedetails.eatpoopyoucat.data

import java.io.Serializable

data class Coordinates(private var xValue: Float, private var yValue: Float) : Serializable {
    val x: Float
        get() = xValue
    val y: Float
        get() = yValue

    constructor(
        x: Float,
        y: Float,
        sourceResolution: Resolution,
        destinationResolution: Resolution
    ) : this(x, y) {
        val xCoefficient: Float =
            (sourceResolution.width).toFloat() / (destinationResolution.width).toFloat()

        val yCoefficient: Float = if (sourceResolution.height == sourceResolution.width)
            xCoefficient
        else
            (sourceResolution.height).toFloat() / (destinationResolution.height).toFloat()

        this.xValue = x / xCoefficient
        this.yValue = y / yCoefficient
    }
}

data class Resolution(val height: Int, val width: Int)
