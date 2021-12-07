package com.nononsenseapps.feeder.ui.compose.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

@Composable
fun LazyListState.isScrolled(): Boolean {
    return remember(this) {
        derivedStateOf { firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0 }
    }.value
}
