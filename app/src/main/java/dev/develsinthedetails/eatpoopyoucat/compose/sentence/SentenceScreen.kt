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
import dev.develsinthedetails.eatpoopyoucat.compose.draw.Draw
import dev.develsinthedetails.eatpoopyoucat.compose.ui.theme.EatPoopYouCatTheme
import dev.develsinthedetails.eatpoopyoucat.viewmodels.SentenceViewModel

@Composable
fun SentenceScreen(
    modifier: Modifier = Modifier,
    viewModel: SentenceViewModel = hiltViewModel(),
    onNavigateToDraw: (String) -> Unit
) {
    EatPoopYouCatTheme {
        val previousEntry by viewModel.previousEntry.observeAsState()
        val idToSend =
            if (previousEntry?.sequence == 0) previousEntry!!.id.toString() else viewModel.entryId

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
                    Draw(drawingZippedJson = it, isReadOnly = true)
                }
                if (viewModel.isError) {
                    Text(text = "write a more better sentence.", color = Color.Red)
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
                            stringResource(R.string.write_a_funny_sentence),
                            modifier = modifier
                        )
                    },
                )
                Button(onClick = { go() }) {
                    Text(stringResource(R.string.accept))
                }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

