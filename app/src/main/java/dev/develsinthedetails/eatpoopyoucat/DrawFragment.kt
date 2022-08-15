package dev.develsinthedetails.eatpoopyoucat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.databinding.FragmentDrawBinding
import dev.develsinthedetails.eatpoopyoucat.utilities.toByteArray
import dev.develsinthedetails.eatpoopyoucat.utilities.CommonStringNames
import java.util.*

class DrawFragment : Fragment() {

    private var _binding: FragmentDrawBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val args: DrawFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_draw,
            container,
            false
        )

        args.entry.sentence.also { binding.sentenceToDraw.text = it }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shared = requireContext().getSharedPreferences(CommonStringNames.player, Context.MODE_PRIVATE)
        val nickname = shared.getString(CommonStringNames.nickname, getString(R.string.default_nickname))!!
        val playerId = UUID.fromString(shared.getString(CommonStringNames.playerId, getString(R.string.default_nickname))!!)
        binding.playerName = getString(R.string.created_by, nickname)

        binding.btnSend.setOnClickListener {

            val drawing = binding.drawView.getDrawing()
            val entry = Entry(
                id = UUID.randomUUID(),
                sequence = args.entry.sequence + 1,
                drawing = drawing.toByteArray(),
                sentence = null,
                timePassed = 0,
                gameId = args.entry.gameId,
                playerId = playerId
            )
            val directions = DrawFragmentDirections.actionGameFragmentToSentenceFragment(entry)
            this.findNavController().navigate(directions)
        }

        binding.btnErase.setOnClickListener {
            binding.drawView.setErase()
        }

        binding.btnDraw.setOnClickListener {
            binding.drawView.setPen()
        }

        binding.btnClearAll.setOnClickListener {
            binding.drawView.clearCanvas()
        }
        binding.btnUndo.setOnClickListener {
            binding.drawView.undo()
        }

        binding.btnRedo.setOnClickListener {
            binding.drawView.redo()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
