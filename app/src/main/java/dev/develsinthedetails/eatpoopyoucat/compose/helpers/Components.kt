package dev.develsinthedetails.eatpoopyoucat.compose.helpers

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.develsinthedetails.eatpoopyoucat.R
import kotlin.math.floor

@Composable
fun Square(contents: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val fill = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            val screenMax = floor(screenHeight * .8).dp
            Modifier
                .width(screenMax)
                .height(screenMax)
        }

        else -> {
            Modifier.fillMaxWidth()
        }
    }
    Box(modifier = fill){
        contents()
    }
}


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

@Preview
@Composable
fun PreviewSpinner() {
    Spinner()
}

@Composable
fun SubmitButton(
    modifier: Modifier = Modifier,
    onSubmit: () -> Unit,
) {
    Button(
        modifier = modifier
            .shadow(15.dp, shape = RoundedCornerShape(50.dp)),
        onClick = onSubmit
    ) {
        Text(stringResource(R.string.submit))
    }
}

@Composable
fun EndGameButton(
    modifier: Modifier = Modifier,
    onEnd: () -> Unit,
) {
    Button(
        modifier = modifier
            .padding(top = 15.dp),
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onSecondaryContainer),
        onClick = onEnd
    )
    {
        Text(stringResource(R.string.end_game_for_all))
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
