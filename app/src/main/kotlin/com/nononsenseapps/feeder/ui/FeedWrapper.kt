package com.nononsenseapps.feeder.ui

import com.nononsenseapps.feeder.db.FeedSQL

class FeedWrapper(val item: FeedSQL?, val tag: String, val isTop: Boolean) {

    @JvmOverloads
    constructor(tag: String, isTop: Boolean = false): this(item = null, tag = tag, isTop = isTop)

    constructor(item: FeedSQL): this(item = item, tag = item.tag, isTop = false)

    val isTag: Boolean
        get() = item == null

    private var _unreadCount: Int = 0
    var unreadCount: Int
        get() = item?.unreadCount ?: _unreadCount
        set(value) {
            _unreadCount = value
        }

    operator fun compareTo(other: FeedWrapper): Int {
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
            item != null && other.item != null && tag == other.tag -> item.displayTitle.compareTo(other.item.displayTitle, ignoreCase = true)
            // In other cases it's just a matter of comparing tags
            else -> tag.compareTo(other.tag, ignoreCase = true)
        }
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> false
            is FeedWrapper -> {
                //val f = other as FeedWrapper?
                if (isTag && other.isTag) {
                    // Compare tags
                    tag == other.tag
                } else {
                    // Compare items
                    !isTag && !other.isTag && item == other.item
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
            item!!.hashCode()
        }
    }

    override fun toString(): String {
        return if (isTag) {
            "Tag: " + tag
        } else {
            "Item: " + item!!.displayTitle + " (" + tag + ")"
        }
    }
}
