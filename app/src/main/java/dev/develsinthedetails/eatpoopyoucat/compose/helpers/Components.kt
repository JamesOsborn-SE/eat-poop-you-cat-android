package dev.develsinthedetails.eatpoopyoucat.compose.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.develsinthedetails.eatpoopyoucat.R

@Composable
fun Spinner(
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun SubmitButton(
    modifier: Modifier = Modifier,
    onSubmit: () -> Unit,
) {
        Button(
            modifier = modifier
                .shadow(8.dp, shape = RoundedCornerShape(50.dp)),
            onClick = onSubmit
        ) {

            Text(stringResource(R.string.submit))
            Spacer(modifier = Modifier.size(5.dp))
            Icon(
                contentDescription = stringResource(id = R.string.submit),
                painter = painterResource(id = R.drawable.ic_send_24),
            )
        }

}

@Composable
fun EndGameButton(
    modifier: Modifier = Modifier,
    onEnd: () -> Unit,
) {
    var showEndGameConfirm by remember { mutableStateOf(false) }
    Button(
        modifier = modifier
            .padding(top = 15.dp),
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSecondaryContainer),
        onClick = { showEndGameConfirm = true }
    )
    {
        Text(stringResource(R.string.end_game_for_all))
        Spacer(modifier = Modifier.size(5.dp))
        Icon(
            contentDescription = stringResource(id = R.string.end_game_for_all),
            painter = painterResource(id = R.drawable.ic_cancel_24),
        )
    }
    if (showEndGameConfirm) {
        ConfirmDialog(
            onDismiss = { showEndGameConfirm = false },
            onConfirm = onEnd,
            action = stringResource(R.string.end_game_for_all)
        )
    }
}

@Composable
fun ErrorText(isError: Boolean, textToDisplay: String) {
    if (isError)
        Text(
            modifier = Modifier.padding(15.dp),
            text = textToDisplay,
            color = MaterialTheme.colorScheme.error,
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDialog(
    action: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(),
        content = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = stringResource(R.string.are_you_sure, action))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = onConfirm) {
                        Text(stringResource(R.string.yes))
                        Spacer(modifier = Modifier.size(5.dp))
                        Icon(
                            contentDescription = stringResource(id = R.string.end_game_for_all),
                            painter = painterResource(id = R.drawable.ic_check_24),
                        )
                    }
                    Button(onClick = onDismiss) {
                        Text(stringResource(R.string.no))
                        Spacer(modifier = Modifier.size(5.dp))
                        Icon(
                            contentDescription = stringResource(id = R.string.end_game_for_all),
                            painter = painterResource(id = R.drawable.ic_cancel_24),
                        )
                    }
                }

            }
        }
    )
}


@Preview
@Composable
fun PreviewSubmit() {
    SubmitButton(onSubmit = {})
}
@Preview
@Composable
fun PreviewEndGame() {
    EndGameButton(onEnd = {})
}

@Preview
@Composable
fun PreviewSpinner() {
    Spinner()
}

@Preview
@Composable
fun PreviewConfirmDialog() {
    ConfirmDialog(
        action = "Oof",
        onDismiss = {},
        onConfirm = {},
    )
}