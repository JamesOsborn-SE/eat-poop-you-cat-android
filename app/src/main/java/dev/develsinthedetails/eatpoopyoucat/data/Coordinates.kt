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
        sourceScreenResolution: ScreenResolution,
        destinationScreenResolution: ScreenResolution
    ) : this(x, y) {
        val xCoefficient: Float =
            (sourceScreenResolution.width).toFloat() / (destinationScreenResolution.width).toFloat()

        val yCoefficient: Float = if (sourceScreenResolution.height == sourceScreenResolution.width)
            xCoefficient
        else
            (sourceScreenResolution.height).toFloat() / (destinationScreenResolution.height).toFloat()

        this.xValue = x / xCoefficient
        this.yValue = y / yCoefficient
    }
}

data class ScreenResolution(val width: Int, val height: Int)
