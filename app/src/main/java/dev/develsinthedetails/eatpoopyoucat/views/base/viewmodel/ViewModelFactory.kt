package dev.develsinthedetails.eatpoopyoucat.views.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory {
}

class MainActivityViewModelFactory(private val startingCount: Int) : ViewModelProvider.Factory {


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(startingCount) as T

        }
        throw IllegalArgumentException("Unknown View Model Class")

    }


}