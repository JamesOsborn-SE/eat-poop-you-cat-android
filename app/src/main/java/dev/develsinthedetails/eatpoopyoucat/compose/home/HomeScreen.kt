package dev.develsinthedetails.eatpoopyoucat.compose.home

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.helpers.Spinner
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.ui.theme.secondaryButtonColors
import dev.develsinthedetails.eatpoopyoucat.viewmodels.GreetingViewModel
import java.util.UUID


@Composable
fun HomeScreen(
    viewModel: GreetingViewModel = hiltViewModel(),
    onNavigateToSentence: (String) -> Unit,
    onNavigateToPreviousGames: () -> Unit,
    onNavigateToCredits: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToContinuePreviousGames: () -> Unit,
) {
    HomeScreen(
        isLoading = viewModel.isLoading,
        onStartGame = {
            val entryId = UUID.randomUUID()
            viewModel.saveNewGame(
                entryId
            ) { onNavigateToSentence(entryId.toString()) }
        },
        onNavigateToPreviousGames = onNavigateToPreviousGames,
        onNavigateToCredits = onNavigateToCredits,
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        onNavigateToContinuePreviousGames = onNavigateToContinuePreviousGames,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onStartGame: () -> Unit,
    onNavigateToPreviousGames: () -> Unit,
    onNavigateToContinuePreviousGames: () -> Unit,
    onNavigateToCredits: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
) {
    val padding = 20.dp
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(ScrollState(0)),
        color = MaterialTheme.colorScheme.background
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
                Text(
                    text = stringResource(R.string.welcome_message),
                    modifier = defaultModifier,
                    fontSize = 18.sp
                )
                StartGame(defaultModifier, onStartGame)
                ViewPreviousGames(defaultModifier, onNavigateToPreviousGames)
                ContinuePreviousGames(defaultModifier, onNavigateToContinuePreviousGames)
                Text(
                    text = stringResource(id = R.string.app_description),
                    modifier = defaultModifier
                )
                Text(
                    text = stringResource(id = R.string.app_warning),
                    modifier = defaultModifier,
                    fontSize = 12.sp
                )

                TextButton(
                    modifier = defaultModifier,
                    onClick = onNavigateToCredits,
                ) {
                    Text(stringResource(id = R.string.about))
                }
                TextButton(
                    modifier = defaultModifier,
                    onClick = onNavigateToPrivacyPolicy
                ) {
                    Text(stringResource(id = R.string.privacy_policy))
                }
            }
        }
    }
}

@Composable
fun ViewPreviousGames(modifier: Modifier, navTo: () -> Unit) {
    Button(
        modifier = modifier,
        colors = secondaryButtonColors(),
        onClick = {
            navTo()
        }) {
        Text(stringResource(id = R.string.previous_games))
        Spacer(modifier = Modifier.size(5.dp))
        Icon(
            contentDescription = null,
            painter = painterResource(id = R.drawable.ic_history_24),
        )
    }
}
@Composable
fun ContinuePreviousGames(modifier: Modifier, navTo: () -> Unit) {
    Button(
        modifier = modifier,
        colors = secondaryButtonColors(),
        onClick = {
            navTo()
        }) {
        Text(stringResource(id = R.string.continue_previous_game))
        Spacer(modifier = Modifier.size(5.dp))
        Icon(
            Icons.Rounded.Replay,
            contentDescription = null,
        )
    }
}
@Composable
fun StartGame(
    modifier: Modifier,
    onStartGame: () -> Unit,
) {
    Button(
        onClick = onStartGame,
        modifier = modifier
    ) {
        Text(stringResource(id = R.string.dialog_start_game))
        Spacer(modifier = Modifier.size(5.dp))
        Icon(
            contentDescription = stringResource(id = R.string.dialog_start_game),
            painter = painterResource(id = R.drawable.ic_start_24),
        )
    }
}

/**
 * Preview Screenshot #1
 */
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = "spec:parent=Nexus 7 2013,orientation=landscape")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:parent=Nexus 7 2013,orientation=landscape"
)
@Composable
fun PreviewHomeScreen() {
    AppTheme {
        HomeScreen(
            isLoading = false,
            onStartGame = {},
            onNavigateToPreviousGames = {},
            onNavigateToCredits = {},
            onNavigateToContinuePreviousGames = {},
        ) {}
    }
}