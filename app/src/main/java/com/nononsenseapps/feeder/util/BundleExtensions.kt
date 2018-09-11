package com.nononsenseapps.feeder.util

import android.os.Bundle
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.ARG_FEED_URL
import org.joda.time.DateTime

inline fun bundle(init: Bundle.() -> Unit): Bundle {
    val bundle = Bundle()
    bundle.init()
    return bundle
}

const val ARG_TITLE = "title"
const val ARG_DESCRIPTION = "body"
const val ARG_LINK = "link"
const val ARG_ENCLOSURE = "enclosure"
const val ARG_IMAGEURL = "imageUrl"
const val ARG_ID = "dbid"
const val ARG_FEEDTITLE = "feedtitle"
const val ARG_AUTHOR = "author"
const val ARG_DATE = "date"

fun Bundle.asFeedItemFoo(): FeedItemWithFeed {
    return FeedItemWithFeed(id = getLong(ARG_ID, ID_UNSET),
            title = getString(ARG_TITLE, ""),
            description = getString(ARG_DESCRIPTION, ""),
            link = getString(ARG_LINK),
            enclosureLink = getString(ARG_ENCLOSURE),
            imageUrl = getString(ARG_IMAGEURL),
            author = getString(ARG_AUTHOR),
            feedTitle = getString(ARG_FEEDTITLE, ""),
            feedUrl = sloppyLinkToStrictURL(getString(ARG_FEED_URL, "")),
            pubDate = when(getString(ARG_DATE)) {
                null -> null
                else -> {
                    var dt: DateTime? = null
                    try {
                        dt = DateTime.parse(getString(ARG_DATE))
                    } catch(t: Throwable) {}
                    dt
                }
            })
}

fun Bundle.setLong(pair: Pair<String, Long>) {
    putLong(pair.first, pair.second)
}

fun Bundle.setString(pair: Pair<String, String?>) {
    putString(pair.first, pair.second)
}

fun Bundle.setBoolean(pair: Pair<String, Boolean>) {
    putBoolean(pair.first, pair.second)
}
