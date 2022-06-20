package dev.develsinthedetails.eatpoopyoucat.data

import android.graphics.Path
import java.io.Serializable

class LineSegment(val start: Coordinates, private val end: Coordinates): Serializable {

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

