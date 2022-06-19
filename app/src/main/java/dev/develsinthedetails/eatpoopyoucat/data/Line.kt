package dev.develsinthedetails.eatpoopyoucat.data

import android.graphics.Path

class Line(lineSegments: List<LineSegment>){
    private val _lineSegments = lineSegments
    fun toPath(): Path {
        val path = Path()
        if(_lineSegments.isNotEmpty()) {
            path.reset()
            path.moveTo(_lineSegments[0].start.x, _lineSegments[0].start.y)
        }
        for (lineSegment in _lineSegments){
            lineSegment.toPath(path)
        }
        return path
    }
}