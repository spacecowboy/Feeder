package com.nononsenseapps.feeder.ui.compose.readaloud

import androidx.compose.runtime.Immutable
import java.time.LocalDate

class PlaylistViewModel

@Immutable
data class PlayableArticle(
    val id: Long,
    val title: String,
    val feedName: String,
    val imageUrl: String?,
    val playedSeconds: Int,
    val totalSeconds: Int,
    val pubDate: LocalDate,
    val currentlyPlaying: Boolean,
)
