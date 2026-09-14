package dev.develsinthedetails.eatpoopyoucat.core.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformLazyVerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier
) {
    VerticalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(listState)
    )
}

@Composable
actual fun PlatformColumnVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier
) {
    VerticalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(scrollState)
    )
}