package dev.develsinthedetails.eatpoopyoucat.feature.setup

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lan
import androidx.compose.material.icons.rounded.NetworkWifi
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
fun StartLocalGame(
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
            Icons.Rounded.PhoneAndroid,
            contentDescription = stringResource(id = R.string.dialog_start_game),
        )
    }
}

@Composable
fun StartLanGame(
    modifier: Modifier,
    onStartGame: () -> Unit,
) {
    Button(
        onClick = onStartGame,
        modifier = modifier
    ) {
        Text("Start Local Game")
        Spacer(modifier = Modifier.size(5.dp))
        Icon(
            Icons.Rounded.Lan,
            contentDescription = stringResource(id = R.string.dialog_start_game),
        )
    }
}

@Composable
fun StartInternetGame(
    modifier: Modifier,
    onStartGame: () -> Unit,
) {
    Button(
        onClick = onStartGame,
        modifier = modifier
    ) {
        Text("Start Internet Game")
        Spacer(modifier = Modifier.size(5.dp))
        Icon(
            Icons.Rounded.NetworkWifi,
            contentDescription = stringResource(id = R.string.dialog_start_game),
        )
    }
}

@Composable
fun NewGameScreen(
    viewModel: NewGameViewModel = koinViewModel(),
    gameId: Uuid,
    nav: NavHostController,
) {

 }

@Composable
fun NewGameScreen(
    onSubmit: () -> Unit,
    onEnd: () -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Scaffolds.InGame(
        title = stringResource(R.string.new_player_prompt),
        onEnd = onEnd
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(ScrollState(0)),
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
            ) {

            }
        }
    }
}
