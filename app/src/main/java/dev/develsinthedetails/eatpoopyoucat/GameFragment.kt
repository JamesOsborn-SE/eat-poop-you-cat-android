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
            val bitmap = binding.drawView.getBitmap()
            var outputSteam = ByteArrayOutputStream(bitmap.byteCount)
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, outputSteam)
            val gzip = Gzip()
            val pngGZipped = gzip.compress(outputSteam.toByteArray())
            var b64encode = Base64.encodeBase64(pngGZipped)
            Log.d("PngGzipBase64Encoded", "bitMap Bytes ${bitmap.byteCount}")
            Log.d("PngGzipBase64Encoded", "png Bytes ${outputSteam.toByteArray().size}")
            Log.d("PngGzipBase64Encoded", "gZipped png Bytes ${pngGZipped.size}")
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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
