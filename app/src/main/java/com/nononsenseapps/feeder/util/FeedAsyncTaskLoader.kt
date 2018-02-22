package com.nononsenseapps.feeder.util

import android.content.Context
import android.database.Cursor
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.util.ArrayMap
import com.nononsenseapps.feeder.db.asFeed
import com.nononsenseapps.feeder.ui.FeedWrapper
import java.util.*


/**
 * Returns a sorted list of *all* feeds and tags correctly sorted in a single list. Filter the result with current
 * visibility to generate an expandable list.
 */
class FeedAsyncTaskLoader(context: Context) : AsyncTaskLoader<List<FeedWrapper>>(context) {
    // Checks for changes in the database
    val observer: ForceLoadContentObserver = ForceLoadContentObserver()
    var cursor: Cursor? = null

    /**
     * Subclasses must implement this to take care of loading their data,
     * as per [.startLoading].  This is not called by clients directly,
     * but as a result of a call to [.startLoading].
     */
    override fun onStartLoading() {
        super.onStartLoading()
        forceLoad()
    }

    /**
     * Subclasses must implement this to take care of stopping their loader,
     * as per [.stopLoading].  This is not called by clients directly,
     * but as a result of a call to [.stopLoading].
     * This will always be called from the process's main thread.
     */
    override fun onStopLoading() {
        cancelLoad()
    }

    /**
     * Called if the task was canceled before it was completed.  Gives the class a chance
     * to clean up post-cancellation and to properly dispose of the result.
     *
     * @param data The value that was returned by [.loadInBackground], or null
     * if the task threw [OperationCanceledException].
     */
    override fun onCanceled(data: List<FeedWrapper>?) {
        cursor?.unregisterContentObserver(observer)
        if (cursor?.isClosed != true) {
            cursor?.close()
        }
    }

    /**
     * Subclasses must implement this to take care of resetting their loader,
     * as per [.reset].  This is not called by clients directly,
     * but as a result of a call to [.reset].
     * This will always be called from the process's main thread.
     */
    override fun onReset() {
        onStopLoading()
        onCanceled(null)
        cursor = null
    }

    override fun loadInBackground(): List<FeedWrapper> {
        val oldCursor = cursor
        cursor = context.contentResolver.cursorForFeedsWithCounts()

        oldCursor?.unregisterContentObserver(observer)
        oldCursor?.close()

        val topTag = FeedWrapper(tag = "", isTop = true)
        val tags: MutableMap<String, FeedWrapper> = ArrayMap()
        tags.put("", topTag)
        val data: MutableList<FeedWrapper> = mutableListOf(topTag)

        if (cursor != null) {
            val it = cursor!!
            // Ensure the cursor window is filled
            it.count
            it.registerContentObserver(observer)

            it.forEach {
                if (!it.isClosed) {
                    val feed = FeedWrapper(item = it.asFeed())

                    if (!tags.contains(feed.tag)) {
                        val tag = FeedWrapper(tag = feed.tag)
                        data.add(tag)
                        tags.put(feed.tag, tag)
                    }

                    topTag.unreadCount += feed.unreadCount
                    // Avoid adding twice for top tag
                    if (feed.tag.isNotEmpty()) {
                        tags[feed.tag]!!.unreadCount += feed.unreadCount
                    }

                    data.add(feed)
                }
            }
        }

        data.sortWith(Comparator { a, b -> a.compareTo(b) })

        return data
    }
}
