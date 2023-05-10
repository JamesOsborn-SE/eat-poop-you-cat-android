package dev.develsinthedetails.eatpoopyoucat.data

import androidx.compose.ui.graphics.Path
import kotlinx.serialization.Serializable

@Serializable
data class Line(
    private val lineSegments: List<LineSegment>,
    val properties: LineProperties = LineProperties(),
    val resolution: Resolution = Resolution(0,0),
) {
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