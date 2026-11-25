package com.nononsenseapps.feeder.db.room

import androidx.compose.runtime.Immutable

@Immutable
data class SelectableFolder(
    val id: Long,
    val name: String,
    val depth: Int,
)
