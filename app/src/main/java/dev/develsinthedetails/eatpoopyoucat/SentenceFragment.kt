//package dev.develsinthedetails.eatpoopyoucat
//
//import android.content.Context
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.View.GONE
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.ViewModelProvider
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import dagger.hilt.android.AndroidEntryPoint
//import dev.develsinthedetails.eatpoopyoucat.R.*
//import dev.develsinthedetails.eatpoopyoucat.data.Entry
//import dev.develsinthedetails.eatpoopyoucat.utilities.CommonStringNames
//import dev.develsinthedetails.eatpoopyoucat.utilities.fromByteArray
//import dev.develsinthedetails.eatpoopyoucat.views.base.viewmodel.MainViewModel
//import dev.develsinthedetails.eatpoopyoucat.views.base.viewmodel.ViewModelFactory
//import java.util.*
//
//
///**
// * A simple [Fragment] subclass.
// * create an instance of this fragment.
// */
//@AndroidEntryPoint
//class SentenceFragment : Fragment(R.layout.fragment_sentence) {
//    lateinit var createdBy: TextView
//    private lateinit var sentenceToDraw: TextView
//    //private lateinit var viewModel: SentenceViewModel
//    private val viewModel: MainViewModel by viewModels()
//    private lateinit var drawView: DrawView
//    private lateinit var playerId: UUID
//    private val args: SentenceFragmentArgs by navArgs()
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//
//        val factory = ViewModelFactory.getInstance(requireContext())
//        viewModel = ViewModelProvider(this, factory)[SentenceViewModel::class.java]
//
//        val root = inflater.inflate(
//            layout.fragment_sentence,
//            container,
//            false
//        )
//        drawView = root.findViewById(R.id.draw_view)
//        sentenceToDraw = root.findViewById(R.id.sentence_to_draw)
//        createdBy = root.findViewById(R.id.created_by)
//        val send = root.findViewById<Button>(R.id.btn_send)
//
//        send.setOnClickListener {
//            val sentenceToDraw = root.findViewById<TextView>(R.id.sentence_to_draw).text.toString()
//            if (sentenceToDraw.isBlank() ||
//                sentenceToDraw.length < 15
//            ) {
//                val alert = AlertFragment()
//                AlertFragment.displayTextStringId = string.no_really_write_a_funny_sentence
//                alert.show(childFragmentManager, "")
//            } else {
//                val entry = Entry(
//                    UUID.randomUUID(),
//                    playerId = playerId,
//                    sequence = args.entry.sequence + 1,
//                    gameId = args.entry.gameId,
//                    timePassed = 0,
//                    sentence = sentenceToDraw,
//                    drawing = null // todo: add real time here
//                )
//                val directions =
//                    SentenceFragmentDirections.actionSentenceFragmentToGameFragment(entry)
//                this.findNavController().navigate(directions)
//            }
//        }
//        if(args.entry.drawing != null)
//            viewModel.drawing = fromByteArray(args.entry.drawing!!)
//
//        return root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val shared =
//            requireContext().getSharedPreferences(CommonStringNames.player, Context.MODE_PRIVATE)
//        val nickname =
//            shared.getString(CommonStringNames.nickname, getString(string.default_nickname))!!
//        playerId =
//            UUID.fromString(shared.getString(CommonStringNames.playerId, getString(string.default_nickname))!!)
//
//        if (viewModel.drawing == null) {
//            viewModel.drawViewVisibility = GONE
//            viewModel.createdByVisibility = GONE
//            drawView.visibility = viewModel.drawViewVisibility
//            createdBy.visibility = viewModel.createdByVisibility!!
//            viewModel.sentenceToDrawHint = getString(string.write_a_funny_sentence, nickname)
//            sentenceToDraw.hint = viewModel.sentenceToDrawHint
//        } else {
//
//            drawView.setDrawing(viewModel.drawing!!)
//        }
//        viewModel.playerName = getString(string.created_by, nickname)
//
//
//    }
//}
