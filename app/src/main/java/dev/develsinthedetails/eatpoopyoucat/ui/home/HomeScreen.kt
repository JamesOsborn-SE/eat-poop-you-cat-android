package dev.develsinthedetails.eatpoopyoucat.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Start
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.Spinner
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.ui.theme.secondaryButtonColors
import dev.develsinthedetails.eatpoopyoucat.utilities.getBitmapFromVectorDrawable
import dev.develsinthedetails.eatpoopyoucat.viewmodels.GreetingViewModel
import java.util.UUID


@Composable
fun HomeScreen(
    viewModel: GreetingViewModel = hiltViewModel(),
    onNavigateToNickname: (String) -> Unit,
    onNavigateToPreviousGames: () -> Unit,
    onNavigateToCredits: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
) {
    HomeScreen(
        isLoading = viewModel.isLoading,
        useNickNames = viewModel.useNicknames,
        toggleUseNicknames = { viewModel.updateUseNicknames() },
        onStartGame = {
            val entryId = UUID.randomUUID()
            viewModel.saveNewGame(
                entryId
            ) { onNavigateToNickname(entryId.toString()) }
        },
        onNavigateToPreviousGames = onNavigateToPreviousGames,
        onNavigateToCredits = onNavigateToCredits,
        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    useNickNames: Boolean,
    toggleUseNicknames: () -> Unit,
    onStartGame: () -> Unit,
    onNavigateToPreviousGames: () -> Unit,
    onNavigateToCredits: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
) {
    val padding = 10.dp
    Scaffolds.Home(
        title = stringResource(
            id = R.string.welcome_message,
            stringResource(id = R.string.app_name)
        )
    )
    { innerPadding ->


        // A surface container using the 'background' color from the theme
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(ScrollState(0)),
            color = MaterialTheme.colorScheme.primaryContainer
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
                        contentDescription = stringResource(R.string.application_icon), modifier = defaultModifier
                            .size(100.dp)
                            .padding(8.dp)
                            .clip(CircleShape)
                    )
                    Row(modifier = defaultModifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { toggleUseNicknames() }
                        )
                    }){
                        Checkbox(checked = useNickNames, onCheckedChange = { toggleUseNicknames() } )
                        Text(modifier = Modifier.align(Alignment.CenterVertically),text = "Use Nicknames?")
                    }
                    StartGame(defaultModifier, onStartGame)
                    ViewPreviousGames(defaultModifier, onNavigateToPreviousGames)
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
}

@Composable
fun ViewPreviousGames(modifier: Modifier, navTo: () -> Unit) {
    Button(
        modifier = modifier,
        colors = secondaryButtonColors(),
        onClick = {
            navTo()
        }) {
        Text(pluralStringResource(id = R.plurals.previous_games, 2))
        Spacer(modifier = Modifier.size(5.dp))
        Icon(
            Icons.Rounded.History,
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
            Icons.Rounded.Start,
            contentDescription = stringResource(id = R.string.dialog_start_game),
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
            useNickNames = true,
            toggleUseNicknames = {},
            onStartGame = {},
            onNavigateToPreviousGames = {},
            onNavigateToCredits = {},
        ) {}
    }
}