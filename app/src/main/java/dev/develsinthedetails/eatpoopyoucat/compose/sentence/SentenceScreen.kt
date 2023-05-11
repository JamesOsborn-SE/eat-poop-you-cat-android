package dev.develsinthedetails.eatpoopyoucat.compose.sentence

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.Spinner
import dev.develsinthedetails.eatpoopyoucat.compose.draw.DrawBox
import dev.develsinthedetails.eatpoopyoucat.compose.ui.theme.EatPoopYouCatTheme
import dev.develsinthedetails.eatpoopyoucat.viewmodels.SentenceViewModel

@Composable
fun SentenceScreen(
    modifier: Modifier = Modifier,
    viewModel: SentenceViewModel = hiltViewModel(),
    onNavigateToDraw: (String) -> Unit,
    onNavigateToHome: () -> Unit
) {
    EatPoopYouCatTheme {
        val previousEntry by viewModel.previousEntry.observeAsState()
        val isFirstTurn = previousEntry?.sequence == 0
        val idToSend =
            if (isFirstTurn) previousEntry!!.id.toString() else viewModel.entryId

        val sentencePromt = if(isFirstTurn) stringResource(R.string.write_a_funny_sentence) else stringResource(R.string.describe_this_drawing)

        val focusRequester = remember { FocusRequester() }

        fun go() {
            if (!viewModel.checkSentence() && previousEntry != null) {
                viewModel.saveEntry { onNavigateToDraw(idToSend) }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (viewModel.isLoading)
                Spinner()
            Column {

                previousEntry?.drawing?.let {
                    DrawBox(drawingZippedJson = it)
                }

                if (viewModel.isError) {
                    Text(text = stringResource(id = R.string.write_sentence_error), color = Color.Red)
                }
                OutlinedTextField(
                    value = viewModel.sentence,
                    onValueChange = { viewModel.updateSentence(it) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { go() }),
                    modifier = modifier
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
                Button(onClick = { go() }) {
                    Text(stringResource(R.string.accept))
                }

                Button(onClick = {
                    if(isFirstTurn)
                        viewModel.deleteGame()
                    onNavigateToHome()
                })
                {
                    Text(stringResource(R.string.end_game_for_all))
                }

                if (previousEntry?.sequence == 0)
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
            }
        }
    }
}

