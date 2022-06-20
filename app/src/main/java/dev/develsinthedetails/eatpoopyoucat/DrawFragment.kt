package dev.develsinthedetails.eatpoopyoucat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dev.develsinthedetails.eatpoopyoucat.databinding.FragmentDrawBinding

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

        args.sentence.also { binding.sentenceToDraw.text = it }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shared = context!!.getSharedPreferences("player", Context.MODE_PRIVATE)
        val nickname = shared.getString("nickname", "No name")!!
        binding.playerName = getString(R.string.created_by, nickname)

        binding.btnSend.setOnClickListener {

            val drawing = binding.drawView.getDrawing()

            val directions = DrawFragmentDirections.actionGameFragmentToSentenceFragment(drawing)
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
