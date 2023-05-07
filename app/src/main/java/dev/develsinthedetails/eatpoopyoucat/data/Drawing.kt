package dev.develsinthedetails.eatpoopyoucat.data

import android.graphics.Path


@kotlinx.serialization.Serializable
data class Drawing(val lines: List<Line>) {
    fun toPaths() : MutableList<Path>{
        val paths = mutableListOf<Path>()
        for (line in lines)
            paths.add(line.toPath())

        return paths
    }
}

