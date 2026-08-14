package dev.develsinthedetails.eatpoopyoucat.feature.setup

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.Start
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Spinner
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.secondaryButtonColors
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.tertiaryButtonColors
import dev.develsinthedetails.eatpoopyoucat.core.utilities.getBitmapFromVectorDrawable
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid


@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    toNewGame: (Uuid) -> Unit,
    toPreviousGames: () -> Unit,
    toInProgressGames: () -> Unit,
    toCredits: () -> Unit,
    toPrivacyPolicy: () -> Unit,
) {
    HomeScreen(
        isLoading = viewModel.isLoading,
        toNewGame = {
            val entryId = Uuid.random()
            viewModel.saveNewGame(
                entryId
            ) { toNewGame(entryId) }
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
    toNewGame: () -> Unit,
    toPreviousGames: () -> Unit,
    toInProgressGames: () -> Unit,
    toCredits: () -> Unit,
    toPrivacyPolicy: () -> Unit,
) {
    val padding = 10.dp
    Scaffolds.Home(
        title = stringResource(
            id = R.string.welcome_message,
            stringResource(id = R.string.app_name)
        )
    )
    { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
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

                    val appIcon =
                        getBitmapFromVectorDrawable(
                            LocalContext.current,
                            R.mipmap.ic_launcher_round
                        )
                    Image(
                        appIcon.asImageBitmap(),
                        contentDescription = stringResource(R.string.application_icon),
                        modifier = defaultModifier
                            .size(100.dp)
                            .padding(8.dp)
                            .clip(CircleShape)
                    )
                    Button(
                        onClick = toNewGame,
                        modifier = modifier
                            .padding(5.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(stringResource(id = R.string.dialog_start_game))
                        Spacer(modifier = Modifier.size(5.dp))
                        Icon(
                            Icons.Rounded.Start,
                            contentDescription = stringResource(id = R.string.dialog_start_game),
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
                        Text(pluralStringResource(id = R.plurals.previous_games, 2))
                        Spacer(modifier = Modifier.size(5.dp))
                        Icon(
                            Icons.Rounded.History,
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
                            Icons.Rounded.NetworkPing,
                            contentDescription = null,
                        )
                    }
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
                        onClick = toCredits,
                    ) {
                        Text(stringResource(id = R.string.about))
                    }
                    TextButton(
                        modifier = defaultModifier,
                        onClick = toPrivacyPolicy
                    ) {
                        Text(stringResource(id = R.string.privacy_policy))
                    }
                }
            }
        }
    }
}

/**
 * Preview Screenshot #1
 */
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:parent=Nexus 7 2013,orientation=landscape"
)
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeScreen(
            isLoading = false,
            toNewGame = {},
            toInProgressGames = {},
            toPreviousGames = {},
            toCredits = {},
            toPrivacyPolicy = {}
        )
    }
}