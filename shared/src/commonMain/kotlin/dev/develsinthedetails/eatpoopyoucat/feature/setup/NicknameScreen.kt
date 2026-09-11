package dev.develsinthedetails.eatpoopyoucat.feature.setup

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.AppButton
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.ErrorText
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.enter_nickname_prompt
import eatpoopyoucat.shared.generated.resources.ic_person_add_rounded
import eatpoopyoucat.shared.generated.resources.nickname_prompt
import eatpoopyoucat.shared.generated.resources.nicknames
import eatpoopyoucat.shared.generated.resources.oof
import eatpoopyoucat.shared.generated.resources.previous_nicknames
import eatpoopyoucat.shared.generated.resources.that_s_me
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun NicknameColumn(
    nickname: String?,
    previousNicknames: List<String>,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
    nicknameError: String?,
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
                text = stringResource(Res.string.nickname_prompt),
                Modifier.padding(bottom = 24.dp),
                fontSize = 20.sp
            )
        }
        if (previousNicknames.isNotEmpty()) {
            Text(stringResource(Res.string.previous_nicknames))
            Column(modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)) {
                previousNicknames.forEach { message ->
                    Text(message)
                }
            }
        }
        if (nicknameError != null) {
            ErrorText(true, nicknameError)
        }
        Column {
            Row {
                OutlinedTextField(
                    value = nickname ?: "",
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
                            stringResource(Res.string.enter_nickname_prompt),
                            modifier = modifier
                        )
                    },
                )
            }
            Row(modifier = Modifier) {
                AppButton(
                    icon = Res.drawable.ic_person_add_rounded,
                    modifier = modifier.fillMaxWidth(),
                    text = Res.string.that_s_me,
                    iconDescription = Res.string.that_s_me,
                    onClick = onSubmit,
                )
            }
        }
    }
}


@Preview
@Composable
fun NicknamePreview() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = stringArrayResource(Res.array.nicknames).toMutableList()
    listOfNicknames.addAll(stringArrayResource(Res.array.nicknames).toList())
    listOfNicknames.addAll(stringArrayResource(Res.array.nicknames).toList())
    listOfNicknames.addAll(stringArrayResource(Res.array.nicknames).toList())
    AppTheme {
        Surface {
            NicknameColumn(
                stringResource(Res.string.oof),
                listOfNicknames,
                {},
                {}, "",
                focusRequester,
            )
        }
    }
}

@Preview
@Composable
fun NicknamePreviewEmpty() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = stringArrayResource(Res.array.nicknames).toList()
    AppTheme {
        Surface {
            NicknameColumn(
                stringResource(Res.string.oof),
                listOfNicknames,
                {},
                {}, "",
                focusRequester,
            )
        }
    }
}

@Preview
@Composable
fun NicknamePreviewEmptyNobody() {
    val focusRequester = remember { FocusRequester() }
    val listOfNicknames = listOf<String>()
    AppTheme {
        Surface {
            NicknameColumn(
                stringResource(Res.string.oof),
                listOfNicknames,
                {},
                {}, "",
                focusRequester,
            )
        }
    }
}