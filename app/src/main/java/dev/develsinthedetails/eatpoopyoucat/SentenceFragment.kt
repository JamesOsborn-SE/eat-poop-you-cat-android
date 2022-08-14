package dev.develsinthedetails.eatpoopyoucat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dev.develsinthedetails.eatpoopyoucat.R.*
import dev.develsinthedetails.eatpoopyoucat.utilities.CommonStringNames
import dev.develsinthedetails.eatpoopyoucat.utilities.CommonStringNames.Companion.player


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class SentenceFragment : Fragment() {

    private lateinit var sentenceToDraw: TextView
    private lateinit var viewModel: SentenceViewModel
    private lateinit var drawView: DrawView
    lateinit var createdBy: TextView

    private val args: SentenceFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SentenceViewModel::class.java]

        val root = inflater.inflate(
            layout.fragment_sentence,
            container,
            false
        )
        drawView = root.findViewById(R.id.draw_view)
        sentenceToDraw = root.findViewById(R.id.sentence_to_draw)
        createdBy = root.findViewById(R.id.created_by)
        val send = root.findViewById<Button>(R.id.btn_send)
        val sentenceToDraw = root.findViewById<EditText>(R.id.sentence_to_draw)
        send.setOnClickListener {
            if (sentenceToDraw.text.toString().isBlank() ||
                sentenceToDraw.text.toString().length < 15
            ) {
                val alert = AlertFragment()
                AlertFragment.displayTextStringId = string.no_really_write_a_funny_sentence
                alert.show(childFragmentManager, "")
            } else {
                val directions =
                    SentenceFragmentDirections.actionSentenceFragmentToGameFragment(sentenceToDraw.text.toString())
                this.findNavController().navigate(directions)
            }
        }
        viewModel.drawing = args.drawing

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shared = requireContext().getSharedPreferences(player, Context.MODE_PRIVATE)
        val nickname = shared.getString(CommonStringNames.nickname, getString(string.default_nickname))!!

        if (viewModel.drawing == null) {
            viewModel.drawViewVisibility = GONE
            viewModel.createdByVisibility = GONE
            drawView.visibility = viewModel.drawViewVisibility
            createdBy.visibility = viewModel.createdByVisibility!!
            viewModel.sentenceToDrawHint = getString(string.write_a_funny_sentence, nickname)
            sentenceToDraw.hint = viewModel.sentenceToDrawHint
        } else {

            drawView.setDrawing(viewModel.drawing!!)
        }
        viewModel.playerName = getString(string.created_by, nickname)


    }
}
