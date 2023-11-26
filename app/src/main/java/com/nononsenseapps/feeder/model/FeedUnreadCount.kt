package com.nononsenseapps.feeder.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.nononsenseapps.feeder.db.COL_CURRENTLY_SYNCING
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLNoThrows
import java.net.URL

data class FeedUnreadCount
    @Ignore
    constructor(
        var id: Long = ID_UNSET,
        var title: String = "",
        var url: URL = sloppyLinkToStrictURLNoThrows(""),
        var tag: String = "",
        @ColumnInfo(name = "custom_title")
        var customTitle: String = "",
        var notify: Boolean = false,
        @ColumnInfo(name = COL_CURRENTLY_SYNCING) var currentlySyncing: Boolean = false,
        @ColumnInfo(name = "image_url") var imageUrl: URL? = null,
        @ColumnInfo(name = "unread_count") var unreadCount: Int = 0,
    ) : Comparable<FeedUnreadCount> {
        constructor() : this(id = ID_UNSET)

        val displayTitle: String
            get() = customTitle.ifBlank { title }

        val isTop: Boolean
            get() = id == ID_ALL_FEEDS

        val isTag: Boolean
            get() = id < 1 && tag.isNotEmpty()

        override operator fun compareTo(other: FeedUnreadCount): Int {
            return when {
                // Top tag is always at the top (implies empty tags)
                isTop -> -1
                other.isTop -> 1
                // Feeds with no tags are always last
                isTag && !other.isTag && other.tag.isEmpty() -> -1
                !isTag && other.isTag && tag.isEmpty() -> 1
                !isTag && !other.isTag && tag.isNotEmpty() && other.tag.isEmpty() -> -1
                !isTag && !other.isTag && tag.isEmpty() && other.tag.isNotEmpty() -> 1
                // Feeds with identical tags compare by title
                tag == other.tag -> displayTitle.compareTo(other.displayTitle, ignoreCase = true)
                // In other cases it's just a matter of comparing tags
                else -> tag.compareTo(other.tag, ignoreCase = true)
            }
        }

        override fun equals(other: Any?): Boolean {
            return when (other) {
                null -> false
                is FeedUnreadCount -> {
                    // val f = other as FeedWrapper?
                    if (isTag && other.isTag) {
                        // Compare tags
                        tag == other.tag
                    } else {
                        // Compare items
                        !isTag && !other.isTag && id == other.id
                    }
                }
                else -> false
            }
        }

        override fun hashCode(): Int {
            return if (isTag) {
                // Tag
                tag.hashCode()
            } else {
                // Item
                id.hashCode()
            }
        }
    }
