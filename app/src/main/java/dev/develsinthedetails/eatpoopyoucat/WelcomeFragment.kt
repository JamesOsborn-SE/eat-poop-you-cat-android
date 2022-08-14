package dev.develsinthedetails.eatpoopyoucat

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.develsinthedetails.eatpoopyoucat.databinding.FragmentWelcomeBinding
import dev.develsinthedetails.eatpoopyoucat.utilities.CommonStringNames
import java.util.*


/**
 * Welcome screen set nickname start a game or whatever
 */

class WelcomeFragment : Fragment() {
    private var welcomeMessage: String =""
    private lateinit var nickname: String

    private lateinit var shared: SharedPreferences
    private var _binding: FragmentWelcomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        shared = requireContext().getSharedPreferences(CommonStringNames.player, Context.MODE_PRIVATE)
        nickname = shared.getString(CommonStringNames.nickname, CommonStringNames.default_nickname)!!

        showNickname()

        if (nickname !== CommonStringNames.default_nickname)
            binding.editNickname.setText(nickname)

        if (shared.getString(CommonStringNames.playerId, CommonStringNames.Empty) ===  CommonStringNames.Empty)
            setPlayerId()

        binding.save.setOnClickListener {
            setNickname()
        }

        binding.editNickname.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                setNickname()
                this.activity.dismissKeyboard()
                true
            } else {
                false
            }
        }

        shared.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key === "nickname")
                getString(
                    R.string.welcome_message,
                    sharedPreferences.getString(CommonStringNames.nickname, CommonStringNames.default_nickname)
                ).also { binding.welcomeMessage.text = it }
        }

        binding.newGame.setOnClickListener {
            val directions = WelcomeFragmentDirections.actionWelcomeToSentenceFragment(null)
            findNavController().navigate(directions)
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun showNickname() {
        binding.welcome = getString(R.string.welcome_message, nickname)
    }

    private fun setNickname() {
        val edit = shared.edit()
        nickname = binding.editNickname.text.toString()
        edit.putString(CommonStringNames.nickname, nickname)
        edit.apply()
        showNickname()
        activity.dismissKeyboard()
    }

    private fun setPlayerId() {
        val edit = shared.edit()
        edit.putString(CommonStringNames.playerId, UUID.randomUUID().toString())
        edit.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
