package dev.develsinthedetails.eatpoopyoucat.core.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformLazyVerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
)

@Composable
expect fun PlatformColumnVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
)