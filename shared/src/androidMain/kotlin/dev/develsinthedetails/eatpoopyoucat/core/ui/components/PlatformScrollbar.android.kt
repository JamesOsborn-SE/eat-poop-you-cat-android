package dev.develsinthedetails.eatpoopyoucat.core.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformLazyVerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier
) {
    // No-op: Mobile users expect standard touch scrolling
}

@Composable
actual fun PlatformColumnVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier
) {
    // No-op
}