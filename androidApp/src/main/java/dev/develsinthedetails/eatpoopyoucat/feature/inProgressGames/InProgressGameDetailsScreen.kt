package dev.develsinthedetails.eatpoopyoucat.feature.inProgressGames


import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.develsinthedetails.eatpoopyoucat.R
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.CustomRoundedPolygon
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.PixelArtImage
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Scaffolds
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.Spinner
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.generateOrganicProfile
import dev.develsinthedetails.eatpoopyoucat.core.ui.components.generatePixelProfile4Bit
import dev.develsinthedetails.eatpoopyoucat.core.ui.theme.AppTheme
import dev.develsinthedetails.eatpoopyoucat.core.utilities.GameMode
import dev.develsinthedetails.eatpoopyoucat.core.utilities.PIXEL_PALETTE_4_BIT
import dev.develsinthedetails.eatpoopyoucat.core.utilities.localDateTimestamp
import dev.develsinthedetails.eatpoopyoucat.data.models.Game
import dev.develsinthedetails.eatpoopyoucat.data.models.Roster
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.SelectableReadOnlyTextWithShare
import dev.develsinthedetails.eatpoopyoucat.feature.netPlay.getShareLink
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Composable
fun InProgressGameDetailsScreen(
    viewModel: InProgressGameDetailsViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val game by viewModel.game.collectAsState(null)
    val players: List<Roster>? by viewModel.players.collectAsState(null)
    InProgressGameDetailsScreen(
        game = game,
        players = players,
        playerId = viewModel.playerId,
        onBack = onBack
    )
}

@Composable
fun InProgressGameDetailsScreen(
    game: Game?, players: List<Roster>?, playerId: Uuid, onBack: () -> Unit
) {
    Scaffolds.Backable("Network game", onBack = onBack) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 15.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            val turns = players?.count { it.sequence >= 0 } ?: 0
            if (game == null || players == null) {
                Spinner()
                return@Surface
            }
            val thisRosterPlayer = players.first { it.playerId == playerId }
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .fillMaxSize()
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                    ) {
                        Row {
                            when (game.gameMode) {
                                GameMode.LAN -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lan),
                                        contentDescription = "Share text",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(end = 10.dp)
                                    )
                                }

                                GameMode.INET -> Icon(
                                    painter = painterResource(id = R.drawable.ic_wifi),
                                    contentDescription = "Share text",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .padding(end = 10.dp)
                                )

                                else -> Icon(
                                    painter = painterResource(id = R.drawable.ic_question_mark),
                                    contentDescription = "Share text"
                                )
                            }
                            val generatedProfile = generateOrganicProfile(game.id)
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
                            when (players.any {
                                it.playerId == playerId && it.sequence >= 0
                            }) {
                                true -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check_circle),
                                        contentDescription = "player had a turn",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(end = 10.dp, start = 10.dp),
                                        tint = Color.Green
                                    )
                                }

                                false -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_schedule),
                                        contentDescription = "player has not had a turn",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(end = 10.dp, start = 10.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Started: ${game.createdAt.localDateTimestamp()}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Turns: $turns",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        when (players.any {
                            it.playerId == playerId && it.sequence >= 0
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
                                    stringResource(R.string.deeplink_scheme) + "://" +
                                            stringResource(R.string.deeplink_host) +
                                            stringResource(R.string.deeplink_play),
                                    thisRosterPlayer.address,
                                    game.id
                                )
                            )
                        }
                        if (turns == 0 && players.size==1 ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_cake),
                                contentDescription = "Waiting for players. the cake is a lie",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(end = 10.dp, start = 10.dp).fillMaxWidth()
                                    .align(Alignment.CenterHorizontally)
                            )
                        }

                    }
                }
                itemsIndexed(players.filter { it.sequence >= 0 }
                    .sortedBy { it.sequence }) { index, player ->
                    RosterPlayerItem(index, player, playerId)
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
                itemsIndexed(players.filter { it.sequence < 0 }
                    .sortedBy { it.sequence }) { index, rosterPlayer ->
                    RosterPlayerItem(index, rosterPlayer, playerId)
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp), thickness = 5.dp
                    )
                }
            }
        }
    }
}

@Composable
fun RosterPlayerItem(index: Int, player: Roster, playerId: Uuid) {
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
        if (player.sequence >= 0 && player.sequence % 2 == 0) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sms),
                contentDescription = "Sentence Turn",
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 10.dp, start = 10.dp)
            )
        } else if (player.sequence >= 0) {
            Icon(
                painter = painterResource(id = R.drawable.ic_draw_rounded),
                contentDescription = "Draw Turn",
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 10.dp, start = 10.dp)
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_schedule),
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
//@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun InProgressGameDetailsPreview() {
    val playerId = Uuid.random()
    val gameId = Uuid.random()
    val game = Game(
        gameId,
        timeout = 100,
        turns = null,
        createdAt = Instant.fromEpochSeconds(1786057118),
        gameMode = GameMode.LAN
    )
    val roster = listOf(
        Roster(
            gameId,
            Uuid.random(),
            "Bob",
            "http://127.0.0.1:3459",
            0,
            false,
            Clock.System.now()
        ),
        Roster(
            gameId,
            Uuid.random(),
            "Bob2",
            "http://127.0.0.1:3459",
            1,
            false,
            Clock.System.now()
        ),
        Roster(
            gameId,
            playerId,
            "Me",
            "http://127.0.0.1:3459",
            2,
            true,
            Clock.System.now()
        ),
        Roster(
            gameId,
            Uuid.random(),
            "Bob4",
            "http://127.0.0.1:3459",
            3,
            false,
            Clock.System.now()
        ),
        Roster(
            gameId,
            Uuid.random(),
            "Bob45",
            "http://127.0.0.1:3459",
            4,
            false,
            Clock.System.now()
        ),
        Roster(
            gameId,
            Uuid.random(),
            "Bob31251",
            "http://127.0.0.1:3459",
            5,
            false,
            Clock.System.now()
        ),
        Roster(
            gameId,
            Uuid.random(),
            "Frank",
            "http://127.0.0.1:3459",
            -1,
            false,
            Clock.System.now()
        ),
        Roster(
            gameId,
            Uuid.random(),
            "Frank1",
            "http://127.0.0.1:3459",
            -1,
            false,
            Clock.System.now()
        ),
        Roster(
            gameId,
            Uuid.random(),
            "Frank2",
            "http://127.0.0.1:3459",
            -1,
            false,
            Clock.System.now()
        ),
        Roster(
            gameId,
            Uuid.random(),
            "Frank5",
            "http://127.0.0.1:3459",
            -1,
            false,
            Clock.System.now()
        ),
        Roster(
            gameId,
            Uuid.random(),
            "Frank55",
            "http://127.0.0.1:3459",
            -1,
            false,
            Clock.System.now()
        ),
    )

    AppTheme {
        InProgressGameDetailsScreen(game, roster, playerId, onBack = {})
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.PIXEL, apiLevel = 37)
fun InProgressGameDetailsSoloPreview() {
    val playerId = Uuid.random()
    val gameId = Uuid.random()
    val game = Game(
        gameId,
        timeout = 100,
        turns = null,
        createdAt = Instant.fromEpochSeconds(1786057118),
        gameMode = GameMode.LAN
    )
    val roster = listOf(
        Roster(
            gameId,
            playerId,
            "Me",
            "http://127.0.0.1:3459",
            -1,
            true,
            Clock.System.now()
        ),
    )

    AppTheme {
        InProgressGameDetailsScreen(game, roster, playerId, onBack = {})
    }
}