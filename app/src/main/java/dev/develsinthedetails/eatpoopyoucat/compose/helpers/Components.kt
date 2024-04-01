package dev.develsinthedetails.eatpoopyoucat.compose.helpers

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.ui.theme.primaryButtonColors
import dev.develsinthedetails.eatpoopyoucat.ui.theme.secondaryButtonColors

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
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
        ) {
            CircularProgressIndicator(
                progress = { 0.89f },
            )
        }
    }
}

@Composable
fun SpinnerScreen(
) {
    AppTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Spinner()
        }
    }
}

@Composable
fun SubmitButton(
    modifier: Modifier = Modifier,
    onSubmit: () -> Unit,
) {
    AppButton(
        modifier = modifier,
        text = R.string.submit,
        iconDescription = R.string.submit,
        onClick = onSubmit,
        icon = R.drawable.ic_send_24
    )
}

@Composable
fun ErrorText(isError: Boolean, textToDisplay: String, errorDetails: String = "") {

    if (isError) {
        Column(modifier = Modifier.padding(top = 15.dp, bottom = 15.dp)) {
            Text(
                text = textToDisplay,
                color = MaterialTheme.colorScheme.error,
            )
            if (errorDetails.isNotBlank())
                Text(
                    text = errorDetails,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp
                )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDialog(
    action: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = { onDismiss() },
        modifier = Modifier.fillMaxWidth(),
        properties = DialogProperties(),
        content = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Icon(
                        contentDescription = stringResource(id = R.string.end_game_for_all),
                        painter = painterResource(id = R.drawable.ic_warning_24),
                        tint = MaterialTheme.colorScheme.inverseSurface,
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                    Text(
                        text = stringResource(R.string.are_you_sure, action),
                        modifier = Modifier.align(Alignment.Bottom)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AppButton(
                        text = R.string.yes,
                        iconDescription = R.string.end_game_for_all,
                        onClick = onConfirm,
                        icon = R.drawable.ic_check_24
                    )
                    AppButton(
                        text = R.string.no,
                        iconDescription = R.string.no,
                        onClick = onDismiss,
                        icon = R.drawable.ic_undo_black_24,
                        colors = secondaryButtonColors()
                    )
                }
            }
        }
    )
}

@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    @StringRes text: Int? = null,
    @DrawableRes icon: Int? = null,
    @StringRes iconDescription: Int? = null,
    enabled: Boolean = true,
    colors: ButtonColors = primaryButtonColors(),
    elevation: ButtonElevation = ButtonDefaults.elevatedButtonElevation()
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        elevation = elevation,
    ) {
        if (text != null)
            Text(stringResource(id = text))

        if (icon != null && iconDescription != null) {
            Icon(
                modifier = Modifier.padding(start = 5.dp),
                contentDescription = stringResource(id = iconDescription),
                painter = painterResource(id = icon),
            )
        }
    }
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewSubmit() {
    AppTheme {
        SubmitButton { }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewSpinner() {
    AppTheme {
        Spinner()
    }
}


@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewSpinnerScreen() {
    AppTheme {
        SpinnerScreen()
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewConfirmDialog() {
    AppTheme {
        Surface {
            ConfirmDialog(
                action = "Oof",
                onDismiss = {},
                onConfirm = {},
            )
        }
    }
}