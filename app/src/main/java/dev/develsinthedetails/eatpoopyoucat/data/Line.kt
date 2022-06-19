package dev.develsinthedetails.eatpoopyoucat.data

import android.graphics.Path

class Line(private val lineSegments: List<LineSegment>){
    fun toPath(): Path {
        val path = Path()
        if(lineSegments.isNotEmpty()) {
            path.reset()
            path.moveTo(lineSegments[0].start.x, lineSegments[0].start.y)
        }
        for (lineSegment in lineSegments){
            lineSegment.toPath(path)
        }
        return path
    }
}