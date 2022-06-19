package dev.develsinthedetails.eatpoopyoucat.data

import android.graphics.Path

class LineSegment(val start: Coordinates, private val end: Coordinates) {

    fun toPath(path: Path) {
        return if (start.x == end.x && start.y == start.y)
            path.lineTo(start.x, start.y)
        else
            path.quadTo(
                start.x,
                start.y,
                (end.x + start.x) / 2,
                (end.y + start.y) / 2
            )
    }
}

