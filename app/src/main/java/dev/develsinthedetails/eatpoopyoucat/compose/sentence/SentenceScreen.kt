package dev.develsinthedetails.eatpoopyoucat.compose.sentence

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.draw.DrawBox
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.ConfirmDialog
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.EndGameButton
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.ErrorText
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.OrientationSwapper
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.Spinner
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.SubmitButton
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.utilities.Gzip
import dev.develsinthedetails.eatpoopyoucat.viewmodels.SentenceViewModel

@Composable
fun SentenceScreen(
    viewModel: SentenceViewModel = hiltViewModel(),
    onNavigateToDraw: (String) -> Unit,
    onNavigateToEndedGame: (String) -> Unit
) {
    val previousEntry by viewModel.previousEntry.observeAsState()
    val isFirstTurn = previousEntry?.sequence == 0
    val idToSend =
        if (isFirstTurn) previousEntry!!.id.toString() else viewModel.entryId
    val sentencePromt =
        if (isFirstTurn) stringResource(R.string.write_a_funny_sentence) else stringResource(R.string.write_a_sentence_to_describe_this_drawing)

    val context = LocalContext.current
    val toastText = stringResource(id = R.string.pass_to_the_next)
    fun submit() {
        if (!viewModel.checkSentence() && previousEntry != null) {
            viewModel.saveEntry { onNavigateToDraw(idToSend) }
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        }
    }

    val onEndGame = { onNavigateToEndedGame(viewModel.previousEntry.value?.gameId.toString()) }

    SentenceScreen(
        isLoading = viewModel.isLoading,
        isError = viewModel.isError,
        isFirstTurn = isFirstTurn,
        sentence = viewModel.sentence,
        sentencePromt = sentencePromt,
        drawing = previousEntry?.drawing,
        onEndGame = onEndGame,
        onSentenceChange = { viewModel.updateSentence(it) },
        onDeleteGame = { viewModel.deleteGame() },
        onSubmit = { submit() }
    )
}

@Composable
fun SentenceScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isError: Boolean = false,
    isFirstTurn: Boolean = false,
    sentence: String = String(),
    sentencePromt: String,
    drawing: ByteArray? = null,
    onEndGame: () -> Unit,
    onSentenceChange: (String) -> Unit,
    onDeleteGame: () -> Unit,
    onSubmit: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    AppTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(ScrollState(0)),
            color = MaterialTheme.colorScheme.background
        ) {
            var showEndGameConfirm by remember { mutableStateOf(false) }

            BackHandler(
                enabled = true
            ) {
                showEndGameConfirm = true
            }
            if (showEndGameConfirm) {
                ConfirmDialog(
                    onDismiss = { showEndGameConfirm = false },
                    onConfirm = onEndGame,
                    action = stringResource(R.string.end_game_for_all)
                )
            }
            if (isLoading)
                Spinner()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp),
            ) {
                ErrorText(isError, stringResource(id = R.string.write_sentence_error))
                Row(modifier = Modifier) {
                    OutlinedTextField(
                        value = sentence,
                        onValueChange = onSentenceChange,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onSubmit() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(.6f)
                            .focusRequester(focusRequester),
                        enabled = true,
                        readOnly = false,
                        shape = RoundedCornerShape(8.dp),

                        label = {
                            Text(
                                sentencePromt,
                                modifier = modifier
                            )
                        },
                    )
                    SubmitButton(
                        modifier = Modifier
                            .weight(.4f)
                            .padding(top = 15.dp, start = 15.dp),
                        onSubmit = onSubmit
                    )
                }
                OrientationSwapper(
                    modifier = Modifier.fillMaxSize(),
                    rowModifier = Modifier.fillMaxSize(),
                    flip = false,
                    {
                        drawing?.let {
                            DrawBox(drawingZippedJson = it)
                        }
                    }
                )
            }

            ConstraintLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                val button = createRef()

                EndGameButton(modifier = Modifier.constrainAs(button) {
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                },
                    onEnd = {
                        if (isFirstTurn)
                            onDeleteGame()
                        onEndGame()
                    })
            }
        }
        if (isFirstTurn)
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
    }
}

/**
 * Preview Screenshot #2
 */
@Preview
@Composable
fun PreviewSentenceScreen() {
    SentenceScreen(
        sentence = dev.develsinthedetails.eatpoopyoucat.utilities.catSentence,
        sentencePromt = stringResource(id = R.string.write_a_funny_sentence),
        drawing = null,
        isError = true,
        onEndGame = {},
        onSentenceChange = {},
        onDeleteGame = {},
        onSubmit = {},
    )
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewSentenceScreenLandscape() {
    PreviewSentenceScreen()
}

/**
 * Preview Screenshot #4
 */
@Preview
@Composable
fun PreviewSentenceScreenWithDrawing() {
    val lines =
        """[{"lineSegments":[{"start":{"xValue":283.71786,"yValue":462.68066},"end":{"xValue":283.71786,"yValue":462.68066}},{"start":{"xValue":283.71786,"yValue":462.68066},"end":{"xValue":273.11176,"yValue":411.20276}},{"start":{"xValue":273.11176,"yValue":411.20276},"end":{"xValue":269.7308,"yValue":388.33832}},{"start":{"xValue":269.7308,"yValue":388.33832},"end":{"xValue":268.09418,"yValue":360.79388}},{"start":{"xValue":268.09418,"yValue":360.79388},"end":{"xValue":268.05933,"yValue":331.3191}},{"start":{"xValue":268.05933,"yValue":331.3191},"end":{"xValue":269.1744,"yValue":301.21088}},{"start":{"xValue":269.1744,"yValue":301.21088},"end":{"xValue":271.09225,"yValue":274.9099}},{"start":{"xValue":271.09225,"yValue":274.9099},"end":{"xValue":275.72525,"yValue":252.3269}},{"start":{"xValue":275.72525,"yValue":252.3269},"end":{"xValue":279.4292,"yValue":233.12802}},{"start":{"xValue":279.4292,"yValue":233.12802},"end":{"xValue":281.7197,"yValue":221.04614}},{"start":{"xValue":281.7197,"yValue":221.04614},"end":{"xValue":281.7197,"yValue":213.0329}},{"start":{"xValue":281.7197,"yValue":213.0329},"end":{"xValue":282.71878,"yValue":206.69437}},{"start":{"xValue":282.71878,"yValue":206.69437},"end":{"xValue":283.71786,"yValue":201.42917}},{"start":{"xValue":283.71786,"yValue":201.42917},"end":{"xValue":284.71692,"yValue":196.40652}},{"start":{"xValue":284.71692,"yValue":196.40652},"end":{"xValue":284.71692,"yValue":193.36154}},{"start":{"xValue":284.71692,"yValue":193.36154},"end":{"xValue":284.71692,"yValue":191.80609}},{"start":{"xValue":284.71692,"yValue":191.80609},"end":{"xValue":289.31894,"yValue":193.6084}},{"start":{"xValue":289.31894,"yValue":193.6084},"end":{"xValue":303.388,"yValue":201.81448}},{"start":{"xValue":303.388,"yValue":201.81448},"end":{"xValue":323.25897,"yValue":219.3711}},{"start":{"xValue":323.25897,"yValue":219.3711},"end":{"xValue":342.08365,"yValue":241.2355}},{"start":{"xValue":342.08365,"yValue":241.2355},"end":{"xValue":356.87656,"yValue":264.61206}},{"start":{"xValue":356.87656,"yValue":264.61206},"end":{"xValue":372.08572,"yValue":289.36053}},{"start":{"xValue":372.08572,"yValue":289.36053},"end":{"xValue":387.86395,"yValue":312.38}},{"start":{"xValue":387.86395,"yValue":312.38},"end":{"xValue":399.98096,"yValue":330.80908}},{"start":{"xValue":399.98096,"yValue":330.80908},"end":{"xValue":410.00873,"yValue":349.54865}},{"start":{"xValue":410.00873,"yValue":349.54865},"end":{"xValue":420.85037,"yValue":371.53198}},{"start":{"xValue":420.85037,"yValue":371.53198},"end":{"xValue":429.42288,"yValue":390.39398}},{"start":{"xValue":429.42288,"yValue":390.39398},"end":{"xValue":436.06467,"yValue":404.59058}},{"start":{"xValue":436.06467,"yValue":404.59058},"end":{"xValue":445.02112,"yValue":417.15442}},{"start":{"xValue":445.02112,"yValue":417.15442},"end":{"xValue":451.2705,"yValue":426.82074}},{"start":{"xValue":451.2705,"yValue":426.82074},"end":{"xValue":455.2707,"yValue":432.11816}},{"start":{"xValue":455.2707,"yValue":432.11816},"end":{"xValue":457.5569,"yValue":436.07538}},{"start":{"xValue":457.5569,"yValue":436.07538},"end":{"xValue":458.55597,"yValue":437.69226}},{"start":{"xValue":458.55597,"yValue":437.69226},"end":{"xValue":461.5532,"yValue":440.69086}},{"start":{"xValue":461.5532,"yValue":440.69086},"end":{"xValue":461.5532,"yValue":440.69086}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":457.5569,"yValue":433.6941},"end":{"xValue":457.5569,"yValue":433.6941}},{"start":{"xValue":457.5569,"yValue":433.6941},"end":{"xValue":508.74945,"yValue":422.00616}},{"start":{"xValue":508.74945,"yValue":422.00616},"end":{"xValue":522.1651,"yValue":421.69965}},{"start":{"xValue":522.1651,"yValue":421.69965},"end":{"xValue":538.36816,"yValue":423.35608}},{"start":{"xValue":538.36816,"yValue":423.35608},"end":{"xValue":560.0069,"yValue":426.352}},{"start":{"xValue":560.0069,"yValue":426.352},"end":{"xValue":575.35284,"yValue":429.42236}},{"start":{"xValue":575.35284,"yValue":429.42236},"end":{"xValue":583.8671,"yValue":431.3806}},{"start":{"xValue":583.8671,"yValue":431.3806},"end":{"xValue":591.47144,"yValue":434.05298}},{"start":{"xValue":591.47144,"yValue":434.05298},"end":{"xValue":595.78424,"yValue":436.37054}},{"start":{"xValue":595.78424,"yValue":436.37054},"end":{"xValue":599.0906,"yValue":437.35718}},{"start":{"xValue":599.0906,"yValue":437.35718},"end":{"xValue":601.116,"yValue":438.38397}},{"start":{"xValue":601.116,"yValue":438.38397},"end":{"xValue":608.41724,"yValue":440.69086}},{"start":{"xValue":608.41724,"yValue":440.69086},"end":{"xValue":608.41724,"yValue":440.69086}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":608.41724,"yValue":428.6964},"end":{"xValue":608.41724,"yValue":428.6964}},{"start":{"xValue":608.41724,"yValue":428.6964},"end":{"xValue":656.6773,"yValue":360.44464}},{"start":{"xValue":656.6773,"yValue":360.44464},"end":{"xValue":670.8209,"yValue":335.09143}},{"start":{"xValue":670.8209,"yValue":335.09143},"end":{"xValue":688.1192,"yValue":309.05072}},{"start":{"xValue":688.1192,"yValue":309.05072},"end":{"xValue":705.7364,"yValue":283.64966}},{"start":{"xValue":705.7364,"yValue":283.64966},"end":{"xValue":718.36523,"yValue":264.69757}},{"start":{"xValue":718.36523,"yValue":264.69757},"end":{"xValue":728.8113,"yValue":242.7714}},{"start":{"xValue":728.8113,"yValue":242.7714},"end":{"xValue":737.0611,"yValue":222.88422}},{"start":{"xValue":737.0611,"yValue":222.88422},"end":{"xValue":745.3561,"yValue":207.97879}},{"start":{"xValue":745.3561,"yValue":207.97879},"end":{"xValue":752.45483,"yValue":195.18622}},{"start":{"xValue":752.45483,"yValue":195.18622},"end":{"xValue":755.6517,"yValue":188.0662}},{"start":{"xValue":755.6517,"yValue":188.0662},"end":{"xValue":758.6671,"yValue":183.42096}},{"start":{"xValue":758.6671,"yValue":183.42096},"end":{"xValue":760.72205,"yValue":179.91989}},{"start":{"xValue":760.72205,"yValue":179.91989},"end":{"xValue":761.56274,"yValue":179.52448}},{"start":{"xValue":761.56274,"yValue":179.52448},"end":{"xValue":761.2757,"yValue":180.81119}},{"start":{"xValue":761.2757,"yValue":180.81119},"end":{"xValue":761.96094,"yValue":183.1818}},{"start":{"xValue":761.96094,"yValue":183.1818},"end":{"xValue":766.4268,"yValue":192.11783}},{"start":{"xValue":766.4268,"yValue":192.11783},"end":{"xValue":772.0249,"yValue":208.0068}},{"start":{"xValue":772.0249,"yValue":208.0068},"end":{"xValue":777.6318,"yValue":227.64581}},{"start":{"xValue":777.6318,"yValue":227.64581},"end":{"xValue":782.58215,"yValue":254.72366}},{"start":{"xValue":782.58215,"yValue":254.72366},"end":{"xValue":787.53253,"yValue":287.6515}},{"start":{"xValue":787.53253,"yValue":287.6515},"end":{"xValue":790.59326,"yValue":319.8324}},{"start":{"xValue":790.59326,"yValue":319.8324},"end":{"xValue":792.92865,"yValue":350.64893}},{"start":{"xValue":792.92865,"yValue":350.64893},"end":{"xValue":794.9184,"yValue":381.82648}},{"start":{"xValue":794.9184,"yValue":381.82648},"end":{"xValue":796.2433,"yValue":407.38782}},{"start":{"xValue":796.2433,"yValue":407.38782},"end":{"xValue":797.2424,"yValue":429.24884}},{"start":{"xValue":797.2424,"yValue":429.24884},"end":{"xValue":797.8853,"yValue":450.4784}},{"start":{"xValue":797.8853,"yValue":450.4784},"end":{"xValue":798.93054,"yValue":464.818}},{"start":{"xValue":798.93054,"yValue":464.818},"end":{"xValue":800.60114,"yValue":474.08008}},{"start":{"xValue":800.60114,"yValue":474.08008},"end":{"xValue":803.55804,"yValue":482.6537}},{"start":{"xValue":803.55804,"yValue":482.6537},"end":{"xValue":806.9105,"yValue":488.0232}},{"start":{"xValue":806.9105,"yValue":488.0232},"end":{"xValue":808.8898,"yValue":491.32568}},{"start":{"xValue":808.8898,"yValue":491.32568},"end":{"xValue":809.23126,"yValue":493.13068}},{"start":{"xValue":809.23126,"yValue":493.13068},"end":{"xValue":811.22943,"yValue":495.6654}},{"start":{"xValue":811.22943,"yValue":495.6654},"end":{"xValue":811.22943,"yValue":495.6654}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":298.70398,"yValue":442.68994},"end":{"xValue":298.70398,"yValue":442.68994}},{"start":{"xValue":298.70398,"yValue":442.68994},"end":{"xValue":301.7012,"yValue":479.41095}},{"start":{"xValue":301.7012,"yValue":479.41095},"end":{"xValue":301.7012,"yValue":502.0805}},{"start":{"xValue":301.7012,"yValue":502.0805},"end":{"xValue":300.99753,"yValue":520.9927}},{"start":{"xValue":300.99753,"yValue":520.9927},"end":{"xValue":300.70212,"yValue":537.97705}},{"start":{"xValue":300.70212,"yValue":537.97705},"end":{"xValue":301.40063,"yValue":562.0246}},{"start":{"xValue":301.40063,"yValue":562.0246},"end":{"xValue":302.37772,"yValue":582.7192}},{"start":{"xValue":302.37772,"yValue":582.7192},"end":{"xValue":305.04425,"yValue":600.3477}},{"start":{"xValue":305.04425,"yValue":600.3477},"end":{"xValue":309.01276,"yValue":622.1984}},{"start":{"xValue":309.01276,"yValue":622.1984},"end":{"xValue":315.73535,"yValue":640.3752}},{"start":{"xValue":315.73535,"yValue":640.3752},"end":{"xValue":324.46893,"yValue":652.7759}},{"start":{"xValue":324.46893,"yValue":652.7759},"end":{"xValue":331.43192,"yValue":666.10297}},{"start":{"xValue":331.43192,"yValue":666.10297},"end":{"xValue":341.19858,"yValue":682.9399}},{"start":{"xValue":341.19858,"yValue":682.9399},"end":{"xValue":351.1679,"yValue":698.489}},{"start":{"xValue":351.1679,"yValue":698.489},"end":{"xValue":361.25317,"yValue":709.82513}},{"start":{"xValue":361.25317,"yValue":709.82513},"end":{"xValue":373.6607,"yValue":722.5865}},{"start":{"xValue":373.6607,"yValue":722.5865},"end":{"xValue":389.73163,"yValue":736.30774}},{"start":{"xValue":389.73163,"yValue":736.30774},"end":{"xValue":401.65634,"yValue":747.59644}},{"start":{"xValue":401.65634,"yValue":747.59644},"end":{"xValue":412.99005,"yValue":759.61346}},{"start":{"xValue":412.99005,"yValue":759.61346},"end":{"xValue":430.78384,"yValue":772.67194}},{"start":{"xValue":430.78384,"yValue":772.67194},"end":{"xValue":447.80444,"yValue":784.4606}},{"start":{"xValue":447.80444,"yValue":784.4606},"end":{"xValue":461.45496,"yValue":793.1288}},{"start":{"xValue":461.45496,"yValue":793.1288},"end":{"xValue":478.63397,"yValue":801.7494}},{"start":{"xValue":478.63397,"yValue":801.7494},"end":{"xValue":498.63336,"yValue":810.2373}},{"start":{"xValue":498.63336,"yValue":810.2373},"end":{"xValue":512.89716,"yValue":815.1958}},{"start":{"xValue":512.89716,"yValue":815.1958},"end":{"xValue":526.6348,"yValue":818.89636}},{"start":{"xValue":526.6348,"yValue":818.89636},"end":{"xValue":541.8582,"yValue":822.223}},{"start":{"xValue":541.8582,"yValue":822.223},"end":{"xValue":559.73035,"yValue":822.51404}},{"start":{"xValue":559.73035,"yValue":822.51404},"end":{"xValue":576.8177,"yValue":821.0576}},{"start":{"xValue":576.8177,"yValue":821.0576},"end":{"xValue":593.1805,"yValue":816.3611}},{"start":{"xValue":593.1805,"yValue":816.3611},"end":{"xValue":614.97754,"yValue":806.2384}},{"start":{"xValue":614.97754,"yValue":806.2384},"end":{"xValue":637.42334,"yValue":792.68567}},{"start":{"xValue":637.42334,"yValue":792.68567},"end":{"xValue":655.1045,"yValue":777.55225}},{"start":{"xValue":655.1045,"yValue":777.55225},"end":{"xValue":678.5945,"yValue":755.303}},{"start":{"xValue":678.5945,"yValue":755.303},"end":{"xValue":703.4241,"yValue":730.11285}},{"start":{"xValue":703.4241,"yValue":730.11285},"end":{"xValue":720.3918,"yValue":704.4384}},{"start":{"xValue":720.3918,"yValue":704.4384},"end":{"xValue":742.6201,"yValue":668.92914}},{"start":{"xValue":742.6201,"yValue":668.92914},"end":{"xValue":763.9741,"yValue":635.879}},{"start":{"xValue":763.9741,"yValue":635.879},"end":{"xValue":777.38,"yValue":607.0024}},{"start":{"xValue":777.38,"yValue":607.0024},"end":{"xValue":786.9157,"yValue":581.966}},{"start":{"xValue":786.9157,"yValue":581.966},"end":{"xValue":792.5885,"yValue":566.6071}},{"start":{"xValue":792.5885,"yValue":566.6071},"end":{"xValue":794.9315,"yValue":555.2027}},{"start":{"xValue":794.9315,"yValue":555.2027},"end":{"xValue":795.2442,"yValue":542.6437}},{"start":{"xValue":795.2442,"yValue":542.6437},"end":{"xValue":795.2442,"yValue":542.6437}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":460.5541,"yValue":552.63904},"end":{"xValue":460.5541,"yValue":552.63904}},{"start":{"xValue":460.5541,"yValue":552.63904},"end":{"xValue":429.5463,"yValue":551.6395}},{"start":{"xValue":429.5463,"yValue":551.6395},"end":{"xValue":418.48706,"yValue":552.63904}},{"start":{"xValue":418.48706,"yValue":552.63904},"end":{"xValue":410.61044,"yValue":553.63855}},{"start":{"xValue":410.61044,"yValue":553.63855},"end":{"xValue":406.97833,"yValue":554.6381}},{"start":{"xValue":406.97833,"yValue":554.6381},"end":{"xValue":405.15027,"yValue":556.0926}},{"start":{"xValue":405.15027,"yValue":556.0926},"end":{"xValue":406.2636,"yValue":557.29614}},{"start":{"xValue":406.2636,"yValue":557.29614},"end":{"xValue":407.92853,"yValue":561.6234}},{"start":{"xValue":407.92853,"yValue":561.6234},"end":{"xValue":411.59293,"yValue":564.9623}},{"start":{"xValue":411.59293,"yValue":564.9623},"end":{"xValue":420.47684,"yValue":566.63257}},{"start":{"xValue":420.47684,"yValue":566.63257},"end":{"xValue":433.23242,"yValue":564.9686}},{"start":{"xValue":433.23242,"yValue":564.9686},"end":{"xValue":445.31897,"yValue":561.25964}},{"start":{"xValue":445.31897,"yValue":561.25964},"end":{"xValue":454.26352,"yValue":556.2855}},{"start":{"xValue":454.26352,"yValue":556.2855},"end":{"xValue":459.93597,"yValue":552.25793}},{"start":{"xValue":459.93597,"yValue":552.25793},"end":{"xValue":462.2218,"yValue":548.9715}},{"start":{"xValue":462.2218,"yValue":548.9715},"end":{"xValue":462.55228,"yValue":546.9447}},{"start":{"xValue":462.55228,"yValue":546.9447},"end":{"xValue":461.89145,"yValue":545.9807}},{"start":{"xValue":461.89145,"yValue":545.9807},"end":{"xValue":455.8908,"yValue":543.64276}},{"start":{"xValue":455.8908,"yValue":543.64276},"end":{"xValue":453.5606,"yValue":542.6437}},{"start":{"xValue":453.5606,"yValue":542.6437},"end":{"xValue":453.5606,"yValue":542.6437}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":712.321,"yValue":545.6423},"end":{"xValue":712.321,"yValue":545.6423}},{"start":{"xValue":712.321,"yValue":545.6423},"end":{"xValue":667.2364,"yValue":529.8749}},{"start":{"xValue":667.2364,"yValue":529.8749},"end":{"xValue":646.8047,"yValue":537.2769}},{"start":{"xValue":646.8047,"yValue":537.2769},"end":{"xValue":634.7752,"yValue":544.2605}},{"start":{"xValue":634.7752,"yValue":544.2605},"end":{"xValue":628.0985,"yValue":550.5907}},{"start":{"xValue":628.0985,"yValue":550.5907},"end":{"xValue":625.7233,"yValue":554.3161}},{"start":{"xValue":625.7233,"yValue":554.3161},"end":{"xValue":628.5531,"yValue":556.3555}},{"start":{"xValue":628.5531,"yValue":556.3555},"end":{"xValue":638.2876,"yValue":558.3361}},{"start":{"xValue":638.2876,"yValue":558.3361},"end":{"xValue":655.2461,"yValue":556.9498}},{"start":{"xValue":655.2461,"yValue":556.9498},"end":{"xValue":669.1395,"yValue":554.9553}},{"start":{"xValue":669.1395,"yValue":554.9553},"end":{"xValue":675.70416,"yValue":552.96423}},{"start":{"xValue":675.70416,"yValue":552.96423},"end":{"xValue":676.6985,"yValue":552.63904}},{"start":{"xValue":676.6985,"yValue":552.63904},"end":{"xValue":676.3543,"yValue":552.63904}},{"start":{"xValue":676.3543,"yValue":552.63904},"end":{"xValue":671.35895,"yValue":550.63995}},{"start":{"xValue":671.35895,"yValue":550.63995},"end":{"xValue":671.35895,"yValue":550.63995}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":446.56708,"yValue":671.584},"end":{"xValue":446.56708,"yValue":671.584}},{"start":{"xValue":446.56708,"yValue":671.584},"end":{"xValue":367.02954,"yValue":637.15546}},{"start":{"xValue":367.02954,"yValue":637.15546},"end":{"xValue":320.57114,"yValue":619.7994}},{"start":{"xValue":320.57114,"yValue":619.7994},"end":{"xValue":271.86615,"yValue":603.4704}},{"start":{"xValue":271.86615,"yValue":603.4704},"end":{"xValue":226.48749,"yValue":585.5176}},{"start":{"xValue":226.48749,"yValue":585.5176},"end":{"xValue":184.37488,"yValue":571.6068}},{"start":{"xValue":184.37488,"yValue":571.6068},"end":{"xValue":153.83812,"yValue":564.6335}},{"start":{"xValue":153.83812,"yValue":564.6335},"end":{"xValue":153.83812,"yValue":564.6335}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":420.59113,"yValue":703.56915},"end":{"xValue":420.59113,"yValue":703.56915}},{"start":{"xValue":420.59113,"yValue":703.56915},"end":{"xValue":356.99722,"yValue":682.0184}},{"start":{"xValue":356.99722,"yValue":682.0184},"end":{"xValue":289.80188,"yValue":676.8489}},{"start":{"xValue":289.80188,"yValue":676.8489},"end":{"xValue":227.29645,"yValue":678.0235}},{"start":{"xValue":227.29645,"yValue":678.0235},"end":{"xValue":160.96486,"yValue":685.5627}},{"start":{"xValue":160.96486,"yValue":685.5627},"end":{"xValue":79.90657,"yValue":699.571}},{"start":{"xValue":79.90657,"yValue":699.571},"end":{"xValue":79.90657,"yValue":699.571}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":422.58926,"yValue":791.52844},"end":{"xValue":422.58926,"yValue":791.52844}},{"start":{"xValue":422.58926,"yValue":791.52844},"end":{"xValue":293.00195,"yValue":834.9697}},{"start":{"xValue":293.00195,"yValue":834.9697},"end":{"xValue":232.44365,"yValue":864.19495}},{"start":{"xValue":232.44365,"yValue":864.19495},"end":{"xValue":154.24162,"yValue":903.2367}},{"start":{"xValue":154.24162,"yValue":903.2367},"end":{"xValue":134.85568,"yValue":912.4724}},{"start":{"xValue":134.85568,"yValue":912.4724},"end":{"xValue":134.85568,"yValue":912.4724}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":630.39685,"yValue":676.58167},"end":{"xValue":630.39685,"yValue":676.58167}},{"start":{"xValue":630.39685,"yValue":676.58167},"end":{"xValue":712.31836,"yValue":677.8476}},{"start":{"xValue":712.31836,"yValue":677.8476},"end":{"xValue":768.62573,"yValue":682.9418}},{"start":{"xValue":768.62573,"yValue":682.9418},"end":{"xValue":827.7429,"yValue":681.59155}},{"start":{"xValue":827.7429,"yValue":681.59155},"end":{"xValue":896.39197,"yValue":670.936}},{"start":{"xValue":896.39197,"yValue":670.936},"end":{"xValue":963.9641,"yValue":651.9385}},{"start":{"xValue":963.9641,"yValue":651.9385},"end":{"xValue":1032.0,"yValue":623.6062}},{"start":{"xValue":1032.0,"yValue":623.6062},"end":{"xValue":1032.0,"yValue":623.6062}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":627.39966,"yValue":748.54834},"end":{"xValue":627.39966,"yValue":748.54834}},{"start":{"xValue":627.39966,"yValue":748.54834},"end":{"xValue":719.1582,"yValue":756.23425}},{"start":{"xValue":719.1582,"yValue":756.23425},"end":{"xValue":775.0462,"yValue":762.59375}},{"start":{"xValue":775.0462,"yValue":762.59375},"end":{"xValue":833.0331,"yValue":769.4485}},{"start":{"xValue":833.0331,"yValue":769.4485},"end":{"xValue":883.6928,"yValue":776.23645}},{"start":{"xValue":883.6928,"yValue":776.23645},"end":{"xValue":891.1554,"yValue":777.5349}},{"start":{"xValue":891.1554,"yValue":777.5349},"end":{"xValue":891.1554,"yValue":777.5349}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":591.4329,"yValue":746.54926},"end":{"xValue":591.4329,"yValue":746.54926}},{"start":{"xValue":591.4329,"yValue":746.54926},"end":{"xValue":670.58484,"yValue":832.63806}},{"start":{"xValue":670.58484,"yValue":832.63806},"end":{"xValue":724.0702,"yValue":853.91394}},{"start":{"xValue":724.0702,"yValue":853.91394},"end":{"xValue":804.0699,"yValue":886.3988}},{"start":{"xValue":804.0699,"yValue":886.3988},"end":{"xValue":823.2183,"yValue":894.4807}},{"start":{"xValue":823.2183,"yValue":894.4807},"end":{"xValue":823.2183,"yValue":894.4807}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":517.5014,"yValue":665.58673},"end":{"xValue":517.5014,"yValue":665.58673}},{"start":{"xValue":517.5014,"yValue":665.58673},"end":{"xValue":564.3419,"yValue":659.9789}},{"start":{"xValue":564.3419,"yValue":659.9789},"end":{"xValue":574.55804,"yValue":658.58997}},{"start":{"xValue":574.55804,"yValue":658.58997},"end":{"xValue":583.6558,"yValue":657.59045}},{"start":{"xValue":583.6558,"yValue":657.59045},"end":{"xValue":589.3312,"yValue":656.9586}},{"start":{"xValue":589.3312,"yValue":656.9586},"end":{"xValue":592.7588,"yValue":656.5909}},{"start":{"xValue":592.7588,"yValue":656.5909},"end":{"xValue":593.4311,"yValue":656.5909}},{"start":{"xValue":593.4311,"yValue":656.5909},"end":{"xValue":593.4311,"yValue":659.1896}},{"start":{"xValue":593.4311,"yValue":659.1896},"end":{"xValue":591.7686,"yValue":662.916}},{"start":{"xValue":591.7686,"yValue":662.916},"end":{"xValue":589.0599,"yValue":667.6487}},{"start":{"xValue":589.0599,"yValue":667.6487},"end":{"xValue":584.3258,"yValue":672.69714}},{"start":{"xValue":584.3258,"yValue":672.69714},"end":{"xValue":580.7713,"yValue":676.9245}},{"start":{"xValue":580.7713,"yValue":676.9245},"end":{"xValue":577.7769,"yValue":679.9174}},{"start":{"xValue":577.7769,"yValue":679.9174},"end":{"xValue":575.9774,"yValue":682.049}},{"start":{"xValue":575.9774,"yValue":682.049},"end":{"xValue":574.77625,"yValue":683.25073}},{"start":{"xValue":574.77625,"yValue":683.25073},"end":{"xValue":571.1247,"yValue":687.5672}},{"start":{"xValue":571.1247,"yValue":687.5672},"end":{"xValue":568.0973,"yValue":691.9318}},{"start":{"xValue":568.0973,"yValue":691.9318},"end":{"xValue":565.77655,"yValue":695.933}},{"start":{"xValue":565.77655,"yValue":695.933},"end":{"xValue":562.79974,"yValue":699.8904}},{"start":{"xValue":562.79974,"yValue":699.8904},"end":{"xValue":560.77454,"yValue":703.25604}},{"start":{"xValue":560.77454,"yValue":703.25604},"end":{"xValue":558.77386,"yValue":706.25714}},{"start":{"xValue":558.77386,"yValue":706.25714},"end":{"xValue":558.46344,"yValue":707.43555}},{"start":{"xValue":558.46344,"yValue":707.43555},"end":{"xValue":558.46344,"yValue":708.0456}},{"start":{"xValue":558.46344,"yValue":708.0456},"end":{"xValue":556.8644,"yValue":708.56683}},{"start":{"xValue":556.8644,"yValue":708.56683},"end":{"xValue":554.1767,"yValue":708.56683}},{"start":{"xValue":554.1767,"yValue":708.56683},"end":{"xValue":549.1073,"yValue":707.5673}},{"start":{"xValue":549.1073,"yValue":707.5673},"end":{"xValue":545.8045,"yValue":705.2271}},{"start":{"xValue":545.8045,"yValue":705.2271},"end":{"xValue":542.77905,"yValue":702.1719}},{"start":{"xValue":542.77905,"yValue":702.1719},"end":{"xValue":538.2428,"yValue":698.3322}},{"start":{"xValue":538.2428,"yValue":698.3322},"end":{"xValue":532.3509,"yValue":693.4371}},{"start":{"xValue":532.3509,"yValue":693.4371},"end":{"xValue":523.0096,"yValue":686.0905}},{"start":{"xValue":523.0096,"yValue":686.0905},"end":{"xValue":520.02234,"yValue":683.83997}},{"start":{"xValue":520.02234,"yValue":683.83997},"end":{"xValue":517.73956,"yValue":682.698}},{"start":{"xValue":517.73956,"yValue":682.698},"end":{"xValue":516.5522,"yValue":682.104}},{"start":{"xValue":516.5522,"yValue":682.104},"end":{"xValue":514.50415,"yValue":679.58026}},{"start":{"xValue":514.50415,"yValue":679.58026},"end":{"xValue":514.50415,"yValue":679.58026}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":543.47736,"yValue":700.57056},"end":{"xValue":543.47736,"yValue":700.57056}},{"start":{"xValue":543.47736,"yValue":700.57056},"end":{"xValue":540.9029,"yValue":734.0159}},{"start":{"xValue":540.9029,"yValue":734.0159},"end":{"xValue":535.8612,"yValue":745.4845}},{"start":{"xValue":535.8612,"yValue":745.4845},"end":{"xValue":524.7331,"yValue":758.625}},{"start":{"xValue":524.7331,"yValue":758.625},"end":{"xValue":508.30988,"yValue":771.43774}},{"start":{"xValue":508.30988,"yValue":771.43774},"end":{"xValue":489.97,"yValue":782.6545}},{"start":{"xValue":489.97,"yValue":782.6545},"end":{"xValue":471.13574,"yValue":791.55664}},{"start":{"xValue":471.13574,"yValue":791.55664},"end":{"xValue":458.44928,"yValue":797.2637}},{"start":{"xValue":458.44928,"yValue":797.2637},"end":{"xValue":456.55783,"yValue":798.52515}},{"start":{"xValue":456.55783,"yValue":798.52515},"end":{"xValue":456.55783,"yValue":798.52515}}],"resolution":{"height":1038,"width":1038}},{"lineSegments":[{"start":{"xValue":580.4431,"yValue":706.56775},"end":{"xValue":580.4431,"yValue":706.56775}},{"start":{"xValue":580.4431,"yValue":706.56775},"end":{"xValue":591.76746,"yValue":753.21814}},{"start":{"xValue":591.76746,"yValue":753.21814},"end":{"xValue":595.77734,"yValue":763.2384}},{"start":{"xValue":595.77734,"yValue":763.2384},"end":{"xValue":601.6799,"yValue":770.7945}},{"start":{"xValue":601.6799,"yValue":770.7945},"end":{"xValue":611.51337,"yValue":776.90155}},{"start":{"xValue":611.51337,"yValue":776.90155},"end":{"xValue":625.877,"yValue":781.90186}},{"start":{"xValue":625.877,"yValue":781.90186},"end":{"xValue":641.7261,"yValue":784.2362}},{"start":{"xValue":641.7261,"yValue":784.2362},"end":{"xValue":660.3691,"yValue":784.5317}},{"start":{"xValue":660.3691,"yValue":784.5317},"end":{"xValue":660.3691,"yValue":784.5317}}],"resolution":{"height":1038,"width":1038}}]"""
    SentenceScreen(
        sentencePromt = stringResource(id = R.string.write_a_sentence_to_describe_this_drawing),
        drawing = Gzip.compress(lines),
        isError = true,
        onEndGame = {},
        onSentenceChange = {},
        onDeleteGame = {},
        onSubmit = {},
    )
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewSentenceScreenWithDrawingLandscape() {
    PreviewSentenceScreenWithDrawing()
}

