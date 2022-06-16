package dev.develsinthedetails.eatpoopyoucat

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.develsinthedetails.eatpoopyoucat.databinding.FragmentGameBinding
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayOutputStream


class GameFragment : Fragment() {

    companion object {
        fun newInstance() = GameFragment()
    }

    private var _binding: FragmentGameBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_game,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.setOnClickListener {
            val drawing = binding.drawView.getDrawing()
            Log.d("drawing", drawing.toPaths().toString())
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
