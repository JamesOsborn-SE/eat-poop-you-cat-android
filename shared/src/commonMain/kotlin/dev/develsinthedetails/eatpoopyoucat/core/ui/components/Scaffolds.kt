package dev.develsinthedetails.eatpoopyoucat.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.develsinthedetails.eatpoopyoucat.core.utilities.SystemBackHandler
import eatpoopyoucat.shared.generated.resources.Res
import eatpoopyoucat.shared.generated.resources.app_name
import eatpoopyoucat.shared.generated.resources.backup_games
import eatpoopyoucat.shared.generated.resources.continue_previous_game
import eatpoopyoucat.shared.generated.resources.end_game_for_all
import eatpoopyoucat.shared.generated.resources.ic_arrow_back_rounded
import eatpoopyoucat.shared.generated.resources.ic_more_vert_filled
import eatpoopyoucat.shared.generated.resources.import_games
import eatpoopyoucat.shared.generated.resources.open
import eatpoopyoucat.shared.generated.resources.share_this_game
import eatpoopyoucat.shared.generated.resources.welcome_message
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object Scaffolds {
    @Composable
    fun InGame(
        title: String,
        bottomBar: @Composable () -> Unit = {},
        floatingActionButton: @Composable () -> Unit = {},
        onEnd: () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        var showMenu by remember { mutableStateOf(false) }
        var showEndGameConfirm by remember { mutableStateOf(false) }

        SystemBackHandler(enabled = true) {
            showEndGameConfirm = true
        }

        val extraContent = @Composable { padding: PaddingValues ->
            content(padding)
            if (showEndGameConfirm) {
                ConfirmDialog(
                    onDismiss = { showEndGameConfirm = false },
                    onConfirm = onEnd,
                    action = stringResource(Res.string.end_game_for_all)
                )
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(title, textAlign = TextAlign.Center)
                    },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_more_vert_filled),
                                contentDescription = stringResource(Res.string.open)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                onClick = { showEndGameConfirm = true },
                                text = { Text(stringResource(Res.string.end_game_for_all)) })
                        }
                    },
                )
            },
            bottomBar = bottomBar,
            content = extraContent,
            floatingActionButton = floatingActionButton,
        )
    }

    @Composable
    fun Home(
        title: String,
        bottomBar: @Composable () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(title, textAlign = TextAlign.Center)
                    },
                    actions = {},
                )
            },
            bottomBar = bottomBar,
            content = content
        )
    }

    @Composable
    fun PreviousGames(
        title: String,
        onBackupGames: () -> Unit,
        onBack: () -> Unit,
        onImportGames: (() -> Unit)?, // Replaced ManagedActivityResultLauncher
        bottomBar: @Composable () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        var showMenu by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(title, textAlign = TextAlign.Center)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back_rounded),
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_more_vert_filled),
                                contentDescription = stringResource(Res.string.open)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onImportGames?.invoke()
                                    showMenu = false
                                },
                                text = { Text(stringResource(Res.string.import_games)) })
                            DropdownMenuItem(
                                onClick = {
                                    onBackupGames()
                                    showMenu = false
                                },
                                text = { Text(stringResource(Res.string.backup_games)) })
                        }
                    },
                )
            },
            bottomBar = bottomBar,
            content = content
        )
    }

    @Composable
    fun PreviousGame(
        title: String,
        onContinueGame: () -> Unit,
        onShareGame: () -> Unit,
        onBackupGame: () -> Unit,
        onBack: () -> Unit,
        onImportGame: (() -> Unit)?,
        bottomBar: @Composable () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        var showMenu by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(text = title, textAlign = TextAlign.Center)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back_rounded),
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_more_vert_filled),
                                contentDescription = "open"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onShareGame()
                                    showMenu = false
                                },
                                text = { Text(stringResource(Res.string.share_this_game)) })
                            DropdownMenuItem(
                                onClick = {
                                    onContinueGame()
                                    showMenu = false
                                },
                                text = { Text(stringResource(Res.string.continue_previous_game)) })
                            DropdownMenuItem(
                                onClick = {
                                    onBackupGame()
                                    showMenu = false
                                },
                                text = { Text(stringResource(Res.string.backup_games)) })
                            DropdownMenuItem(
                                onClick = {
                                    onImportGame?.invoke()
                                    showMenu = false
                                },
                                text = { Text(stringResource(Res.string.import_games)) })
                        }
                    },
                )
            },
            bottomBar = bottomBar,
            content = content
        )
    }

    @Composable
    fun Backable(
        title: String,
        onBack: () -> Unit,
        floatingActionButton: @Composable () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(title, textAlign = TextAlign.Center)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back_rounded),
                                contentDescription = "Back"
                            )
                        }
                    },
                )
            },
            floatingActionButton = floatingActionButton,
            content = content
        )
    }
}

@Preview
@Composable
fun HomeBarPreview() {
    Scaffolds.Home(
        title = stringResource(
            Res.string.welcome_message,
            stringResource(Res.string.app_name)
        ), {}) {
    }
}

@Preview
@Composable
fun InGamePreview() {
    Scaffolds.InGame(
        title = stringResource(
            Res.string.welcome_message,
            stringResource(Res.string.app_name)
        ), {}) {
    }
}

@Preview
@Composable
fun BackablePreview() {
    Scaffolds.Backable(
        title = stringResource(
            Res.string.welcome_message,
            stringResource(Res.string.app_name)
        ), {}) {
    }
}