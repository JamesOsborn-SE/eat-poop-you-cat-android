package dev.develsinthedetails.eatpoopyoucat.data

import android.graphics.Path

class Drawing(lines: List<Line>) {
    private val _lines: List<Line> = lines
    fun toPaths() : List<Path>{
        var paths = mutableListOf<Path>()
        for (line in _lines)
            paths.add(line.toPath())

        return paths
    }
}

class Line(lineSegments: List<LineSegment>){
    private val _lineSegments = lineSegments
    fun toPath(): Path {
        var path = Path()
        if(_lineSegments.isNotEmpty()) {
            path.reset()
            path.moveTo(_lineSegments[0].startX, _lineSegments[0].startY)
        }
        for (lineSegment in _lineSegments){
            lineSegment.toPath(path)
        }
        return path
    }
}

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