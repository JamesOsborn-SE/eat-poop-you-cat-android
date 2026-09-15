package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Spinner
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.app_icon_background
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.secondaryButtonColors
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.tertiaryButtonColors
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ManageServerLifecycle
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ServerManager
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.about
import eatpoopyoucat.shared.generated.resources.app_description
import eatpoopyoucat.shared.generated.resources.app_name
import eatpoopyoucat.shared.generated.resources.app_warning
import eatpoopyoucat.shared.generated.resources.application_icon
import eatpoopyoucat.shared.generated.resources.dialog_start_game
import eatpoopyoucat.shared.generated.resources.epyc_icon
import eatpoopyoucat.shared.generated.resources.ic_history_rounded
import eatpoopyoucat.shared.generated.resources.ic_network_ping_rounded
import eatpoopyoucat.shared.generated.resources.ic_start_rounded
import eatpoopyoucat.shared.generated.resources.previous_games
import eatpoopyoucat.shared.generated.resources.privacy_policy
import eatpoopyoucat.shared.generated.resources.use_nicknames
import eatpoopyoucat.shared.generated.resources.use_nicknames_more_info
import eatpoopyoucat.shared.generated.resources.welcome_message
import eatpoopyoucat.shared.generated.resources.what_s_this
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    serverManager: ServerManager = koinInject(),
    toNewGame: () -> Unit,
    toPreviousGames: () -> Unit,
    toInProgressGames: () -> Unit,
    toCredits: () -> Unit,
    toPrivacyPolicy: () -> Unit,
) {
    val useNicknames by viewModel.useNicknames.collectAsStateWithLifecycle(false)
    // todo add setting to toggle this
    ManageServerLifecycle(serverManager, onUpdateAddress = { })
    HomeScreen(
        isLoading = viewModel.isLoading,
        useNickNames = useNicknames,
        toggleUseNicknames = { viewModel.updateUseNicknames(useNicknames) },
        toNewGame = {
            viewModel.saveNewGame(toNewGame)
        },
        toPreviousGames = toPreviousGames,
        toInProgressGames = toInProgressGames,
        toCredits = toCredits,
        toPrivacyPolicy = toPrivacyPolicy,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    useNickNames: Boolean,
    toggleUseNicknames: () -> Unit,
    toNewGame: () -> Unit,
    toPreviousGames: () -> Unit,
    toInProgressGames: () -> Unit,
    toCredits: () -> Unit,
    toPrivacyPolicy: () -> Unit,
) {
    val padding = 10.dp
    var showNicknameMoreInfo by rememberSaveable { mutableStateOf(false) }
    Scaffolds.Home(
        title = stringResource(
            Res.string.welcome_message,
            stringResource(Res.string.app_name)
        )
    )
    { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isLoading)
                    Spinner()
                Column {
                    val defaultModifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(padding)
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(padding)
                    ) {

                        Image(
                            painter = painterResource(Res.drawable.epyc_icon),
                            contentDescription = stringResource(Res.string.application_icon),
                            modifier = defaultModifier
                                .background(
                                    app_icon_background,
                                    shape = CircleShape
                                )
                                .size(140.dp)
                                .padding(15.dp)
                        )
                        Row(modifier = defaultModifier.pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { toggleUseNicknames() }
                            )
                        }) {
                            Checkbox(
                                checked = useNickNames,
                                onCheckedChange = { toggleUseNicknames() })
                            Text(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                text = stringResource(Res.string.use_nicknames)
                            )
                            TextButton(
                                modifier = Modifier.rotate(13f),
                                onClick = { showNicknameMoreInfo = !showNicknameMoreInfo }) {
                                Text(stringResource(Res.string.what_s_this))
                            }
                        }
                        AnimatedVisibility(showNicknameMoreInfo) {
                            Row(modifier = defaultModifier) {
                                Text(stringResource(Res.string.use_nicknames_more_info))
                            }
                        }
                        Button(
                            onClick = toNewGame,
                            modifier = modifier
                                .padding(5.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(Res.string.dialog_start_game))
                            Spacer(modifier = Modifier.size(5.dp))
                            Icon(
                                painter = painterResource(Res.drawable.ic_start_rounded),
                                contentDescription = stringResource(Res.string.dialog_start_game),
                            )
                        }
                        Button(
                            modifier = modifier
                                .padding(5.dp)
                                .align(Alignment.CenterHorizontally),
                            colors = secondaryButtonColors(),
                            onClick = {
                                toPreviousGames()
                            }) {
                            Text(pluralStringResource(Res.plurals.previous_games, 2))
                            Spacer(modifier = Modifier.size(5.dp))
                            Icon(
                                painter = painterResource(Res.drawable.ic_history_rounded),
                                contentDescription = null,
                            )
                        }
                        Button(
                            modifier = modifier
                                .padding(5.dp)
                                .align(Alignment.CenterHorizontally),
                            colors = tertiaryButtonColors(),
                            onClick = {
                                toInProgressGames()
                            }) {
                            Text("In Progress Games")
                            Spacer(modifier = Modifier.size(5.dp))
                            Icon(
                                painter = painterResource(Res.drawable.ic_network_ping_rounded),
                                contentDescription = null,
                            )
                        }
                        Text(
                            text = stringResource(Res.string.app_description),
                            modifier = defaultModifier
                        )
                        Text(
                            text = stringResource(Res.string.app_warning),
                            modifier = defaultModifier,
                            fontSize = 12.sp
                        )

                        TextButton(
                            modifier = defaultModifier,
                            onClick = toCredits,
                        ) {
                            Text(stringResource(Res.string.about))
                        }
                        TextButton(
                            modifier = defaultModifier,
                            onClick = toPrivacyPolicy
                        ) {
                            Text(stringResource(Res.string.privacy_policy))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview Screenshot #1
 */
@Preview
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            isLoading = false,
            useNickNames = false,
            toggleUseNicknames = {},
            toNewGame = {},
            toInProgressGames = {},
            toPreviousGames = {},
            toCredits = {},
            toPrivacyPolicy = {}
        )
    }
}