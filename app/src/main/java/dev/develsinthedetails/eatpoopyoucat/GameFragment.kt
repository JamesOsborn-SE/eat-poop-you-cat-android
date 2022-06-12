package dev.develsinthedetails.eatpoopyoucat

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.databinding.FragmentGameBinding
import dev.develsinthedetails.eatpoopyoucat.data.Game

class GameFragment : Fragment() {

    companion object {
        fun newInstance() = GameFragment()
    }

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentGameBinding>(
            inflater,
            R.layout.fragment_game,
            container,
            false)
        return binding.root
    }

       fun interface Callback {
            fun add(game: Game?)
        }
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        gameViewModel = ViewModelProvider(this).get(GameViewModel::class.java)
//        gameViewModel.addGame()
//        // TODO: Use the ViewModel
//
//    }

}