package dev.develsinthedetails.eatpoopyoucat

import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    private var _sentenceToDraw = "eat poop you cat!"

    val sentenceToDraw
        get() = _sentenceToDraw
}