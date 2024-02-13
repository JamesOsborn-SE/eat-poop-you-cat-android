package dev.develsinthedetails.eatpoopyoucat.compose

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.compose.draw.DrawScreen
import dev.develsinthedetails.eatpoopyoucat.compose.home.HomeScreen
import dev.develsinthedetails.eatpoopyoucat.compose.previousgames.PreviousGameScreen
import dev.develsinthedetails.eatpoopyoucat.compose.previousgames.PreviousGamesScreen
import dev.develsinthedetails.eatpoopyoucat.compose.sentence.SentenceScreen
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.utilities.ReadMetadata

@Composable
fun EatPoopYouCatApp(
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToSentence = {
                    navController.navigate("sentence/${it}") {
                        popUpTo("home")
                    }
                },
                onNavigateToPreviousGames = {
                    navController.navigate("games")
                },
                onNavigateToCredits = {
                    navController.navigate("credits")
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigate("privacypolicy")
                })
        }

        composable(
            "sentence/{EntryId}",
            arguments = listOf(
                navArgument("EntryId") { type = NavType.StringType }
            )
        ) {
            SentenceScreen(
                onNavigateToDraw = {
                    navController.navigate("draw/${it}") {
                        popUpTo("home")
                    }
                },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToEndedGame = {
                    navController.navigate("game/${it}") {
                        popUpTo("home")
                    }
                })
        }
        composable(
            "draw/{EntryId}",
            arguments = listOf(
                navArgument("EntryId") { type = NavType.StringType }
            )
        ) {
            DrawScreen(onNavigateToSentence = {
                navController.navigate("sentence/${it}") {
                    popUpTo("home")
                }
            }, onNavigateToEndedGame = {
                navController.navigate("game/${it}") {
                    popUpTo("home")
                }
            })
        }
        composable(
            "games",
        ) {
            PreviousGamesScreen(
                onGoHome = { navController.navigate("home") },
                onGameClick = { navController.navigate("game/${it}") }
            )
        }
        composable(
            "game/{GameId}",
            arguments = listOf(
                navArgument("GameId") { type = NavType.StringType }
            )
        ) {
            PreviousGameScreen()
        }
        composable("credits") {
            CreditsScreen()
        }
        composable("privacypolicy") {
            PrivacyPolicyScreen()
        }
    }
}

@Composable
fun CreditsScreen() {
    val uriHandler = LocalUriHandler.current

    Surface(
        modifier = Modifier
            .verticalScroll(ScrollState(0))
            .fillMaxSize(),
        color = (MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Text(
                    modifier = Modifier
                        .padding(8.dp),
                    text = ReadMetadata(LocalContext.current).getFullDescription()
                )
            }

            Row {
                TextButton(onClick = {
                    uriHandler.openUri("https://hosted.weblate.org/engage/eat-poop-you-cat-android/")
                }) {
                    Text(
                        text =
                        stringResource(id = R.string.translations_welcome)
                    )
                }
                Row {
                    TextButton(onClick = {
                        uriHandler.openUri("https://github.com/JamesOsborn-SE/eat-poop-you-cat-android/issues")
                    }) {
                        Text(
                            text =
                            stringResource(id = R.string.issues)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen() {
    Surface(
        modifier = Modifier
            .verticalScroll(ScrollState(0))
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                modifier = Modifier
                    .padding(8.dp),
                text = ReadMetadata(LocalContext.current).getPrivacyPolicy()
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = "spec:parent=Nexus 7 2013,orientation=landscape")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:parent=Nexus 7 2013,orientation=landscape"
)
@Composable
fun PreviewCreditsScreen() {
    AppTheme {
        CreditsScreen()
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = "spec:parent=Nexus 7 2013,orientation=landscape")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:parent=Nexus 7 2013,orientation=landscape"
)
@Composable
fun PreviewPrivacyPolicyScreen() {
    AppTheme {
        PrivacyPolicyScreen()
    }
}