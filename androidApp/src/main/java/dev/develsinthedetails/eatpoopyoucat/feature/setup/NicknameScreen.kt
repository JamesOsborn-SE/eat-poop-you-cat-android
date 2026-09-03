package dev.develsinthedetails.eatpoopyoucat.feature.setup

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.AppButton
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.ErrorText
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme

@Composable
fun NicknameColumn(
    nickname: String?,
    previousNicknames: List<String>,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
    nicknameError: Int?,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.nickname_prompt),
                Modifier.padding(bottom = 24.dp),
                fontSize = 20.sp
            )
        }
        if (previousNicknames.isNotEmpty()) {
            Text(stringResource(R.string.previous_nicknames))
            Column(modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)) {
                previousNicknames.forEach { message ->
                    Text(message)
                }
            }
        }
        if (nicknameError != null) {
            ErrorText(true, stringResource(nicknameError))
        }
        Column {
            Row {
                OutlinedTextField(
                    value = nickname?:"",
                    onValueChange = onChange,
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
                            stringResource(R.string.enter_nickname_prompt),
                            modifier = modifier
                        )
                    },
                )
            }
            Row(modifier = Modifier) {
                AppButton(
                    icon = R.drawable.ic_person_add_rounded,
                    modifier = modifier.fillMaxWidth(),
                    text = R.string.that_s_me,
                    iconDescription = R.string.that_s_me,
                    onClick = onSubmit,
                )
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NicknamePreview() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = stringArrayResource(id = R.array.nicknames).toMutableList()
    listOfNicknames.addAll(stringArrayResource(id = R.array.nicknames).toList())
    listOfNicknames.addAll(stringArrayResource(id = R.array.nicknames).toList())
    listOfNicknames.addAll(stringArrayResource(id = R.array.nicknames).toList())
    AppTheme {
        Surface {
            NicknameColumn(
                stringResource(id = R.string.oof),
                listOfNicknames,
                {},
                {}, 0,
                focusRequester,
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NicknamePreviewEmpty() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = stringArrayResource(id = R.array.nicknames).toList()
    AppTheme {
        Surface {
            NicknameColumn(
                stringResource(id = R.string.oof),
                listOfNicknames,
                {},
                {}, 0,
                focusRequester,
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NicknamePreviewEmptyNobody() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = listOf<String>()
    AppTheme {
        Surface {
            NicknameColumn(
                stringResource(id = R.string.oof),
                listOfNicknames,
                {},
                {}, 0,
                focusRequester,
            )
        }
    }
}