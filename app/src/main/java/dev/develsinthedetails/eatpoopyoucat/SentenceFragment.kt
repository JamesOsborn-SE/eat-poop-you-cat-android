package dev.develsinthedetails.eatpoopyoucat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dev.develsinthedetails.eatpoopyoucat.data.Drawing
import dev.develsinthedetails.eatpoopyoucat.databinding.FragmentSentenceBinding

private const val ARG_DRAWING = "drawing"

/**
 * A simple [Fragment] subclass.
 * Use the [SentenceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SentenceFragment : Fragment() {

    private lateinit var drawing: Drawing
    private var _binding: FragmentSentenceBinding? = null
    private val binding get() = _binding!!

    private val args: SentenceFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sentence,
            container,
            false
        )

        drawing = args.drawing

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.drawView.setDrawing(drawing)

        binding.btnSend.setOnClickListener {
            val directions = SentenceFragmentDirections.actionSentenceFragmentToGameFragment(binding.sentenceToDraw.text.toString())
            this.findNavController().navigate(directions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
