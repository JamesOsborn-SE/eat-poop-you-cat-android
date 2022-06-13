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
import dagger.hilt.android.AndroidEntryPoint
import dev.develsinthedetails.eatpoopyoucat.databinding.FragmentGameBinding
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import kotlinx.android.synthetic.main.fragment_game.*
import kotlinx.android.synthetic.main.fragment_game.view.*
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class GameFragment : Fragment() {

    companion object {
        fun newInstance() = GameFragment()
    }

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentGameBinding>(
            inflater,
            R.layout.fragment_game,
            container,
            false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_save.setOnClickListener {
            val bitmap= view.draw_view.getBitmap()
            var outputSteam = ByteArrayOutputStream(bitmap.byteCount)
            val x = bitmap.compress(Bitmap.CompressFormat.PNG, 10, outputSteam)
            val gzip = Gzip()
            val pngGZipped = gzip.compress(outputSteam.toByteArray())
            var b64encode = Base64.encodeBase64(pngGZipped)
            Log.d("PngGzipBase64Encoded",String(b64encode))
            Log.d("PngGzipBase64Encoded","bitMap Bytes ${bitmap.byteCount}")
            Log.d("PngGzipBase64Encoded","png Bytes ${outputSteam.toByteArray().size}")
            Log.d("PngGzipBase64Encoded","gziped png Bytes ${pngGZipped.size}")
        }

        btn_erase.setOnClickListener {
            view.draw_view.setErase()
        }

        btn_draw.setOnClickListener {
            view.draw_view.setPen()
        }

        btn_clearAll.setOnClickListener {
            view.draw_view.clearCanvas()
        }
    }
}