package dev.develsinthedetails.eatpoopyoucat.viewmodels

import android.graphics.Path
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.SharedPref
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import dev.develsinthedetails.eatpoopyoucat.data.Drawing
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.Line
import dev.develsinthedetails.eatpoopyoucat.data.LineSegment
import dev.develsinthedetails.eatpoopyoucat.utilities.fromByteArray
import dev.develsinthedetails.eatpoopyoucat.utilities.toByteArray
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DrawViewModel @Inject constructor(
    state: SavedStateHandle,
    private val repository: AppRepository,
) : ViewModel() {
    var height: Int = 0
    var width: Int = 0
    private val playerId = SharedPref.playerId()
    var isError: Boolean by mutableStateOf(false)
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set

    private val previousEntryId: String = checkNotNull(state.get<String>("EntryId"))
    val previousEntry: LiveData<Entry> = repository.getEntry(previousEntryId).asLiveData()

    val entryId = UUID.randomUUID().toString()

    var drawingPaths: ArrayList<Path> = ArrayList()
    var drawingLines: MutableList<Line> = mutableListOf()
    var undonePaths: ArrayList<Path> = ArrayList()
    var lineSegments: MutableList<LineSegment> = mutableListOf()
    var undoneLines: MutableList<Line> = mutableListOf()
    var isReadOnly: Boolean = false
    var justCleared: Boolean = false

    init {
        clearCanvas()
    }

    private fun clearCanvas() {
        undoneLines.clear()
        undoneLines.addAll(drawingLines)
        undonePaths.clear()
        undonePaths.addAll(drawingPaths)
        justCleared = true
        drawingLines.clear()
        drawingPaths.clear()
    }

    fun checkDrawing(onNavigateToSentence: () -> Unit) {
        if(height == 0 || width == 0)
            isError
        isLoading = true
        val newEntry: Entry = previousEntry.value!!.copy(
            id = UUID.fromString(entryId),
            sentence = null,
            drawing = Drawing(drawingLines).toByteArray(),
            height = height,
            width = width,
            sequence = previousEntry.value!!.sequence.inc(),
            playerId = playerId
        )
        viewModelScope.launch {
            repository.createEntry(newEntry)
            onNavigateToSentence.invoke()
            isLoading = false
        }
    }

    companion object {
        fun fromByteArray(b: ByteArray): java.util.ArrayList<Path> {
            val drawingPaths: java.util.ArrayList<Path> = java.util.ArrayList<Path>()
            val drawing = fromByteArray<Drawing>(b)

            val drawingLines: MutableList<Line> = mutableListOf()
            drawingLines.addAll(drawing.lines)
            drawingPaths.addAll(drawing.toPaths())
            return drawingPaths
        }
    }
}
