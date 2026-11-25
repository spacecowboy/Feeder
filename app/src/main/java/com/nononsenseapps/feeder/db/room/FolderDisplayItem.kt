package com.nononsenseapps.feeder.db.room

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo

@Immutable
data class FolderDisplayItem(
    val id: Long,
    val name: String,
)

@Immutable
data class FeedDisplayItem(
    val id: Long,
    @ColumnInfo(name = "display_title")
    val displayTitle: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
)
