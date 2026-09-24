package com.nononsenseapps.feeder.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.nononsenseapps.feeder.db.room.ID_UNSET
import java.net.URL

data class FeedUnreadCount
    @Ignore
    constructor(
        @ColumnInfo(name = "feed_id")
        override var feedId: Long = ID_UNSET,
        @ColumnInfo(name = "folder_id")
        override var folderId: Long = ID_UNSET,
        @ColumnInfo(name = "display_title")
        var displayTitle: String = "",
        @ColumnInfo(name = "image_url") var imageUrl: URL? = null,
        @ColumnInfo(name = "unread_count") var unreadCount: Int = 0,
        var expanded: Boolean = false,
        var depth: Int = 0,
    ) : FeedIdWithFolderId {
        constructor() : this(feedId = ID_UNSET)
    }

interface FeedIdWithFolderId {
    val feedId: Long
    val folderId: Long
}
