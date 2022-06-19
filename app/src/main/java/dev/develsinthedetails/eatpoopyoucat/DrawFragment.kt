package dev.develsinthedetails.eatpoopyoucat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import dev.develsinthedetails.eatpoopyoucat.databinding.FragmentDrawBinding
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip


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
        binding.btnSend.setOnClickListener {
            val gson = Gson()
            val gzip = Gzip()
            val drawing = binding.drawView.getDrawing()
            val drawingSerialized = gson.toJson(drawing)
            val zipped = gzip.compress(drawingSerialized.toByteArray())

            Log.wtf("drawing", drawingSerialized)
            Log.wtf("drawing", zipped.size.toString())
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
