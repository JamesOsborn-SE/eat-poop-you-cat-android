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
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    var drawingPaths: MutableList<Path> = mutableListOf()
    var drawingLines: MutableList<Line> = mutableListOf()
    var undonePaths: MutableList<Path> = mutableListOf()
    var lineSegments: MutableList<LineSegment> = mutableListOf()
    var undoneLines: MutableList<Line> = mutableListOf()
    var isReadOnly: Boolean = false
    var justCleared: Boolean = false

    init {
        clearCanvas()
        drawingPaths = Drawing(drawingLines).toPaths()
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
        if (height == 0 || width == 0)
            isError
        isLoading = true
        val newEntry: Entry = previousEntry.value!!.copy(
            id = UUID.fromString(entryId),
            sentence = null,
            drawing = Gzip.compress(Json.encodeToString(drawingLines)),
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
}
