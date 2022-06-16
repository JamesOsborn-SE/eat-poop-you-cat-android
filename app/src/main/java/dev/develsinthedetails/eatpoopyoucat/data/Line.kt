package dev.develsinthedetails.eatpoopyoucat.data

import android.graphics.Path

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