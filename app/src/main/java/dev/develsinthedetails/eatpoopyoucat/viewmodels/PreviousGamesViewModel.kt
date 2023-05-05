package dev.develsinthedetails.eatpoopyoucat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.develsinthedetails.eatpoopyoucat.data.AppRepository
import javax.inject.Inject

@HiltViewModel
class PreviousGamesViewModel @Inject constructor(
    repository: AppRepository,
) : ViewModel() {
    val games = repository.getAllGamesWithEntries().asLiveData()
}