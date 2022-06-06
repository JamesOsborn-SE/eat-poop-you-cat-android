package com.example.eatpoopyoucat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatpoopyoucat.data.Game
import com.example.eatpoopyoucat.data.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository
): ViewModel() {

    fun addGame() {
        Log.e("yo","yoyo")
        viewModelScope.launch {
            var guid = UUID.randomUUID()
            val game = Game(guid, null, null)
            var result = gameRepository.createGame(game)
            var games = gameRepository.allGames
            Log.wtf("oof", "addGame: {games}")
        }
    }
}