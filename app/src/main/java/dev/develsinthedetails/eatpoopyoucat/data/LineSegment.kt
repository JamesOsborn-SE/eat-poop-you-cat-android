package dev.develsinthedetails.eatpoopyoucat.data

import android.graphics.Path

class LineSegment(startX: Float, startY: Float, endX: Float, endY: Float) {
    private val _startX = startX
    private val _startY = startY
    private val _endX = endX
    private val _endY = endY
    val startY: Float
        get() {
            return _startY
        }
    val startX: Float
        get() {
            return _startX
        }
    fun toPath(path: Path) {
        return if (_startX == _endX && _startY == _startY)
            path.lineTo(_startX, _startY)
        else
            path.quadTo(
                _startX,
                _startY,
                (_endX + _startX) / 2,
                (_endY + _startY) / 2
            )

    }

}