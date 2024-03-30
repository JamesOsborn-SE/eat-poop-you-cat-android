package dev.develsinthedetails.eatpoopyoucat.data

import androidx.compose.ui.graphics.Path
import kotlinx.serialization.Serializable

@Serializable
data class LineSegment(val start: Coordinates, val end: Coordinates) {

    fun toPath(path: Path) {
        return if (start.x == end.x && start.y == start.y)
            path.lineTo(start.x, start.y)
        else
            path.quadraticBezierTo(
                start.x,
                start.y,
                (end.x + start.x) / 2,
                (end.y + start.y) / 2
            )
    }
}
