package dev.develsinthedetails.eatpoopyoucat.feature.netPlay.inProgressGames


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.CustomRoundedPolygon
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.PixelArtImage
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.PlatformLazyVerticalScrollbar
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Spinner
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.generateOrganicProfile
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.generatePixelProfile4Bit
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.PIXEL_PALETTE_4_BIT
import dev.develsinthedetails.eatpoopyoucat.core.utilities.getShareLink
import dev.develsinthedetails.eatpoopyoucat.core.utilities.localDateTimestamp
import dev.develsinthedetails.eatpoopyoucat.data.models.Entry
import dev.develsinthedetails.eatpoopyoucat.data.models.EntryType
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.NetGame
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.data.models.type
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.SelectableReadOnlyTextWithShare
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ManageServerLifecycle
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.services.ServerManager
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.end_game_for_all
import eatpoopyoucat.shared.generated.resources.ic_cake
import eatpoopyoucat.shared.generated.resources.ic_check_circle
import eatpoopyoucat.shared.generated.resources.ic_draw_rounded
import eatpoopyoucat.shared.generated.resources.ic_lan
import eatpoopyoucat.shared.generated.resources.ic_question_mark
import eatpoopyoucat.shared.generated.resources.ic_schedule
import eatpoopyoucat.shared.generated.resources.ic_sms
import eatpoopyoucat.shared.generated.resources.ic_warning_rounded
import eatpoopyoucat.shared.generated.resources.ic_wifi
import eatpoopyoucat.shared.generated.resources.nicknames
import eatpoopyoucat.shared.generated.resources.scroll_to_top
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Composable
fun InProgressGameDetailsScreen(
    viewModel: InProgressGameDetailsViewModel = koinViewModel(),
    serverManager: ServerManager = koinInject(),
    onBack: () -> Unit,
    onEnd: (Uuid) -> Unit,
) {
    val game by viewModel.game.collectAsState(null)
    val uiState by viewModel.uiState.collectAsState()

    ManageServerLifecycle(
        serverManager = serverManager,
        onUpdateAddress = { viewModel.updateAddress(it) }
    )
    InProgressGameDetailsScreen(
        uiState = uiState,
        game = game,
        myPlayerId = viewModel.playerId,
        onBack = onBack,
        gameOverMan = {
            viewModel.gameOverMan()
            val gameId = game?.game?.id
            if (gameId != null)
                onEnd(gameId)
        }
    )
}

@Composable
fun InProgressGameDetailsScreen(
    uiState: InProgressGamesUiState,
    game: NetGame?, myPlayerId: Uuid, onBack: () -> Unit,
    gameOverMan: () -> Unit
) {
    // todo don't show users in Joined who already took a turn.
    val listState = rememberLazyListState()
    Scaffolds.Backable("Network game", onBack = onBack, floatingActionButton = {
        Row {
            // todo pixel pushing
            FloatingActionButton(
                modifier = Modifier.padding(3.dp),
                onClick = { gameOverMan() }
            ) {
                Row {
                    Icon(
                        painter = painterResource(Res.drawable.ic_warning_rounded),
                        modifier = Modifier.padding(3.dp),
                        contentDescription = stringResource(Res.string.scroll_to_top)
                    )
                    Text(stringResource(Res.string.end_game_for_all))
                }
            }
        }
    }) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 15.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            val turns = game?.entries?.size ?: 0
            if (game == null || game.roster.isEmpty()) {
                Spinner()
                return@Surface
            }
            val thisRosterPlayer = game.roster.first { it.playerId == myPlayerId }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .fillMaxSize(),
                    state = listState,
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                        ) {
                            Row {
                                when (game.game.gameMode) {
                                    GameMode.LAN -> {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_lan),
                                            contentDescription = "Share text",
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(end = 10.dp)
                                        )
                                    }

                                    GameMode.INET -> Icon(
                                        painter = painterResource(Res.drawable.ic_wifi),
                                        contentDescription = "Share text",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(end = 10.dp)
                                    )

                                    else -> Icon(
                                        painter = painterResource(Res.drawable.ic_question_mark),
                                        contentDescription = "Share text"
                                    )
                                }
                                val generatedProfile = generateOrganicProfile(game.game.id)
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(generatedProfile.backgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CustomRoundedPolygon(
                                        generated = generatedProfile,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                when (game.roster.any { player ->
                                    player.playerId == myPlayerId && game.entries.any { it.playerId == myPlayerId }
                                }) {
                                    true -> {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_check_circle),
                                            contentDescription = "player had a turn",
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(end = 10.dp, start = 10.dp),
                                            tint = Color.Green
                                        )
                                    }

                                    false -> {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_schedule),
                                            contentDescription = "player has not had a turn",
                                            modifier = Modifier
                                                .size(50.dp)
                                                .padding(end = 10.dp, start = 10.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Started: ${game.game.createdAt.localDateTimestamp()}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "Turns: $turns",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            when (game.roster.any { player ->
                                player.playerId == myPlayerId && game.entries.any { it.playerId == myPlayerId }
                            }) {
                                true -> {
                                    Text(
                                        text = "Your work here is done",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }

                                false -> {
                                    Text(
                                        text = "Waiting for turn",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                            if (thisRosterPlayer.isLeader) {
                                SelectableReadOnlyTextWithShare(
                                    Modifier.padding(bottom = 15.dp),
                                    getShareLink(
                                        uiState.address,
                                        game.game.id
                                    )
                                )
                            }
                            if (turns == 0 && game.roster.size == 1) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_cake),
                                    contentDescription = "Waiting for players. the cake is a lie",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .padding(end = 10.dp, start = 10.dp).fillMaxWidth()
                                        .align(Alignment.CenterHorizontally)
                                )
                            }

                        }
                    }
                    itemsIndexed(game.entries.sortedBy { it.sequence }) { _, entry ->
                        RosterPlayerItem(entry.sequence, thisRosterPlayer, game.entries, myPlayerId)
                    }
                    item {
                        Text(
                            "Joined",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp),
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp), thickness = 5.dp
                        )
                    }
                    itemsIndexed(game.roster.filter { r ->
                        game.entries.any { r.playerId != it.playerId }
                    }) { index, rosterPlayer ->
                        RosterPlayerItem(index, rosterPlayer, game.entries, myPlayerId)
                    }
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp), thickness = 5.dp
                        )
                    }
                }
                PlatformLazyVerticalScrollbar(
                    listState = listState,
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
fun RosterPlayerItem(index: Int, player: Roster, entries: List<Entry>, playerId: Uuid) {
    val rowColor = if (index % 2 == 0) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.background
    }
    Row(
        modifier = Modifier
            .background(rowColor)
            .fillMaxWidth()
    ) {
        var m = Modifier
            .size(50.dp)
            .rotate(90f)
            .padding(horizontal = 5.dp)
        if (playerId == player.playerId) {
            m = m.dropShadow(
                shape = RoundedCornerShape(3.dp), shadow = Shadow(
                    radius = 4.dp,
                    spread = 2.dp,
                    color = Color.Yellow,
                    offset = DpOffset(x = 0.dp, 0.dp)
                )
            )
        }
        PixelArtImage(
            generatePixelProfile4Bit(player.playerId), PIXEL_PALETTE_4_BIT, m
        )
        if (entries.any { it.playerId == player.playerId && it.type == EntryType.Sentence }) {
            Icon(
                painter = painterResource(Res.drawable.ic_sms),
                contentDescription = "Sentence Turn",
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 10.dp, start = 10.dp)
            )
        } else if (entries.any { it.playerId == player.playerId && it.type == EntryType.Drawing }) {
            Icon(
                painter = painterResource(Res.drawable.ic_draw_rounded),
                contentDescription = "Draw Turn",
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 10.dp, start = 10.dp)
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_schedule),
                contentDescription = "Waiting",
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 10.dp, start = 10.dp)
            )
        }
        Text(
            player.nickname,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
@Preview
fun InProgressGameDetailsPreview() {
    val playerId = Uuid.parse("085900db-809b-408b-b656-62fcfa1c921b")
    val gameId = Uuid.parse("085900db-809b-408b-b656-62fcfa1c921b")
    val nicknames = stringArrayResource(Res.array.nicknames)
    val game = NetGame(
        Game(
            gameId,
            timeout = 100,
            turns = null,
            createdAt = Instant.fromEpochSeconds(1786057118),
            gameMode = GameMode.LAN
        ), entries = (
                listOf(
                    Entry(
                        sequence = 1,
                        id = Uuid.NIL,
                        playerId = playerId,
                        localPlayerName = "TODO()",
                        gameId = gameId,
                        timePassed = 0,
                        sentence = "TODO()",
                        drawing = null,
                        createdAt = Instant.fromEpochSeconds(1786057118),
                    )
                )),
        roster = listOf(
            Roster(
                gameId,
                Uuid.parse("927fb5d6-a27a-48b6-a97c-3494f17e6beb"),
                nicknames[0]
            ),
            Roster(
                gameId,
                Uuid.parse("0d7b2219-6db7-4ab7-a3b1-2ad06169dfc9"),
                nicknames[1]
            ),
            Roster(
                gameId,
                playerId,
                nicknames[2]
            ),
            Roster(
                gameId,
                Uuid.parse("670f27a7-e146-4463-8774-935958c8d298"),
                nicknames[3]
            ),
            Roster(
                gameId,
                Uuid.parse("0ce3fd21-6b6e-41e3-9d4d-547a2f83b281"),
                nicknames[4]
            ),
            Roster(
                gameId,
                Uuid.parse("a81c33fb-c43f-46eb-9e95-f93485906e2e"),
                nicknames[5]
            ),
            Roster(
                gameId,
                Uuid.parse("bc58e47a-6e72-4509-bf4d-7b72b6af813f"),
                nicknames[6]
            ),
            Roster(
                gameId,
                Uuid.parse("6c47151b-c6cc-4d22-8a05-652779d1c72c"),
                nicknames[7]
            ),
        )
    )
    val uiState = InProgressGamesUiState("http://127.0.0.1:3459")
    AppTheme {
        InProgressGameDetailsScreen(uiState, game, playerId, onBack = {}, gameOverMan = {})
    }
}

@Composable
@Preview
fun InProgressGameDetailsSoloPreview() {
    val playerId = Uuid.parse("085900db-809b-408b-b656-62fcfa1c921b")
    val gameId = Uuid.parse("085900db-809b-408b-b656-62fcfa1c921b")
    val game = NetGame(
        Game(
            gameId,
            timeout = 100,
            turns = null,
            createdAt = Instant.fromEpochSeconds(1786057118),
            gameMode = GameMode.LAN
        ), entries = listOf(), roster = listOf(
            Roster(
                gameId,
                playerId,
                "Me"
            ),
        )
    )

    val uiState = InProgressGamesUiState("http://127.0.0.1:3459")
    AppTheme {
        InProgressGameDetailsScreen(uiState, game, playerId, onBack = {}, {})
    }
}