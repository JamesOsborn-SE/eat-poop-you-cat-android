package dev.develsinthedetails.eatpoopyoucat.data

import android.graphics.Path
import java.io.Serializable

data class Drawing(val lines: List<Line>): Serializable {
    fun toPaths() : List<Path>{
        val paths = mutableListOf<Path>()
        for (line in lines)
            paths.add(line.toPath())

        return paths
    }
}

