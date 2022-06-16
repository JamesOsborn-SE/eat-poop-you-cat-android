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

