package dev.develsinthedetails.eatpoopyoucat.ui.previousgames

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VerticalAlignTop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.data.Entry
import dev.develsinthedetails.eatpoopyoucat.data.GameWithEntries
import dev.develsinthedetails.eatpoopyoucat.ui.draw.DrawBox
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.ui.helpers.SpinnerScreen
import dev.develsinthedetails.eatpoopyoucat.utilities.ImageExport
import dev.develsinthedetails.eatpoopyoucat.utilities.getBitmapFromVectorDrawable
import dev.develsinthedetails.eatpoopyoucat.utilities.saveBitmap
import dev.develsinthedetails.eatpoopyoucat.utilities.shareImageUri
import dev.develsinthedetails.eatpoopyoucat.viewmodels.PreviousGameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@Composable
fun PreviousGameScreen(
    modifier: Modifier = Modifier,
    viewModel: PreviousGameViewModel = hiltViewModel(),
    onContinueGame: (Entry) -> Unit = {},
    onBackupGame: (games: List<GameWithEntries>?) -> Unit,
    onImportGames: ManagedActivityResultLauncher<String, Uri?>,
) {
    val game by viewModel.gameWithEntries.observeAsState(initial = null)

    if (game != null) {
        PreviousGameScreen(
            modifier = modifier,
            entries = game!!.entries,
            onContinueGame = { onContinueGame(game!!.entries.last()) },
            onBackupGame = { onBackupGame(listOf(game!!)) },
            onImportGame = onImportGames
        )
    } else
        SpinnerScreen()
}

@Composable
fun PreviousGameScreen(
    modifier: Modifier = Modifier,
    entries: List<Entry>,
    onContinueGame: () -> Unit,
    onBackupGame: () -> Unit,
    onImportGame: ManagedActivityResultLauncher<String, Uri?>?,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val appName = stringResource(id = R.string.app_name)
    val bottomBlurb = stringResource(R.string.is_avalible_on_f_droid_and_google_play, appName)
    val option = BitmapFactory.Options()
    option.inPreferredConfig = Bitmap.Config.ARGB_8888
    val appIcon = getBitmapFromVectorDrawable(LocalContext.current, R.mipmap.ic_launcher_round)

    val shareGame = {
        shareGame(
            coroutineScope,
            entries,
            appIcon,
            appName,
            bottomBlurb,
            context
        )
    }
    Scaffolds.PreviousGame(
        title = stringResource(
            id = R.string.previous_games
        ),
        onBackupGame = onBackupGame,
        onImportGame = onImportGame,
        onShareGame = shareGame(),
        onContinueGame = onContinueGame
    )
    { innerPadding ->
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(floatingActionButton = {
                Row {
                    FloatingActionButton(
                        modifier = Modifier.padding(3.dp),
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }) {

                        Icon(
                            Icons.Rounded.VerticalAlignTop,
                            modifier = Modifier.padding(3.dp),
                            contentDescription = stringResource(id = R.string.scroll_to_top)
                        )
                    }
                    FloatingActionButton(
                        modifier = Modifier.padding(3.dp),
                        onClick = onContinueGame
                    ) {
                        Icon(
                            Icons.Rounded.Replay,
                            modifier = Modifier.padding(3.dp),
                            contentDescription = stringResource(id = R.string.continue_previous_game)
                        )
                    }
                    FloatingActionButton(
                        modifier = Modifier.padding(3.dp),
                        onClick = shareGame()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            modifier = Modifier.padding(3.dp),
                            contentDescription = stringResource(R.string.share_this_game)
                        )
                    }
                }
            },
                content = { contentPadding ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(contentPadding),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(
                            items = entries,
                            key = { entry ->
                                entry.id
                            }
                        ) { entry ->
                            EntryListItem(entry)
                        }
                    }
                })
        }
    }
}

private fun shareGame(
    coroutineScope: CoroutineScope,
    entries: List<Entry>,
    appIcon: Bitmap,
    appName: String,
    bottomBlurb: String,
    context: Context
): () -> Unit = {
    coroutineScope.launch {
        val ie = ImageExport(
            entries,
            appIcon,
            appName,
            bottomBlurb
        )
        val game = saveBitmap(context, ie.makeBitmap())

        if (game != null)
            shareImageUri(context, game)
        else
            Toast.makeText(
                context,
                context.getString(R.string.share_failed),
                Toast.LENGTH_SHORT
            ).show()
    }
}

@Composable
fun EntryListItem(entry: Entry) {
    val sentence = entry.sentence
    val drawing = entry.drawing
    val playerName = entry.localPlayerName
    val createdAt =
        if (entry.createdAt != null) SimpleDateFormat().format(entry.createdAt) else null

    if (sentence != null) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)) {
            Text(
                text = sentence,
                textAlign = TextAlign.Start,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start)
                    .padding(
                        PaddingValues(
                            start = 8.dp,
                            top = 16.dp,
                            end = 8.dp,
                            bottom = 12.dp
                        )
                    )
            )
        }
    }

    if (drawing != null) {
        DrawBox(
            drawingZippedJson = drawing
        )
    }
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (playerName != null)
                Text(text = "^^ $playerName", modifier = Modifier.padding(end = 16.dp))
            if (createdAt != null)
                Text(createdAt)
        }
    }
}

@Preview(device = "spec:parent=Nexus 7 2013")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = "spec:parent=Nexus 7 2013,orientation=landscape")
@Preview(
    device = "spec:parent=Nexus 7 2013,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviousGameScreenPreviewWrapper() {
    PreviousGameScreenPreview()
}
