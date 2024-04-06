package com.nononsenseapps.feeder.db.room

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView(
    value = """
    select feeds.id as feed_id, item_id, case when custom_title is '' then title else custom_title end as display_title, tag, image_url, unread, bookmarked
    from feeds
    left join (
        select id as item_id, feed_id, read_time is null as unread, bookmarked
        from feed_items
        where not exists(select 1 from blocklist where lower(feed_items.plain_title) glob blocklist.glob_pattern)
    )
    ON feeds.id = feed_id
    """,
    viewName = "feeds_with_items_for_nav_drawer",
)
data class FeedsWithItemsForNavDrawer(
    @ColumnInfo(name = "feed_id")
    val feedId: Long,
    val tag: String,
    @ColumnInfo(name = "display_title")
    val displayTitle: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    val unread: Boolean,
    @ColumnInfo(name = "item_id")
    val itemId: Long?,
    val bookmarked: Boolean,
)
