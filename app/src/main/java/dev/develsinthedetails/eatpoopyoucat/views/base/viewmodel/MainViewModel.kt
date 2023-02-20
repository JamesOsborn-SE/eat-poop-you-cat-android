package dev.develsinthedetails.eatpoopyoucat.views.base.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.data.AppRepositoryImpl
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.Game
import dev.develsinthedetails.eatpoopyoucat.data.Player
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: AppRepositoryImpl): ViewModel(){
    suspend fun createPlayer(player: Player){
        return repository.createPlayer(player)
    }
    suspend fun getGame(id: UUID){
        return repository.getGame(id)
    }
    suspend fun createGame(game: Game){
        return repository.createGame(game)
    }
    suspend fun createEntry(entry: Entry){
        return repository.createEntry(entry)
    }
    suspend fun getEntry(id: UUID){
        return repository.getEntry(id)
    }
}