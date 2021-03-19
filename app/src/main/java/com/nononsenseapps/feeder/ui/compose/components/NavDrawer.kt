package com.nononsenseapps.feeder.ui.compose.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun TopLevelFeed(text: String = "Some feed", unreadCount: Int = 99) {
    Text(text)
}
