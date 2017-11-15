package com.nononsenseapps.feeder.model

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import android.content.OperationApplicationException
import android.net.Uri
import android.os.RemoteException
import com.nononsenseapps.feeder.db.AUTHORITY
import com.nononsenseapps.feeder.db.COL_AUTHOR
import com.nononsenseapps.feeder.db.COL_DESCRIPTION
import com.nononsenseapps.feeder.db.COL_ENCLOSURELINK
import com.nononsenseapps.feeder.db.COL_FEED
import com.nononsenseapps.feeder.db.COL_FEEDTITLE
import com.nononsenseapps.feeder.db.COL_GUID
import com.nononsenseapps.feeder.db.COL_IMAGEURL
import com.nononsenseapps.feeder.db.COL_LINK
import com.nononsenseapps.feeder.db.COL_PLAINSNIPPET
import com.nononsenseapps.feeder.db.COL_PLAINTITLE
import com.nononsenseapps.feeder.db.COL_PUBDATE
import com.nononsenseapps.feeder.db.COL_TAG
import com.nononsenseapps.feeder.db.COL_TITLE
import com.nononsenseapps.feeder.db.COL_URL
import com.nononsenseapps.feeder.db.Cleanup
import com.nononsenseapps.feeder.db.FEED_FIELDS
import com.nononsenseapps.feeder.db.FeedSQL
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.URI_FEEDS
import com.nononsenseapps.feeder.db.Util.LongsToStringArray
import com.nononsenseapps.feeder.db.Util.ToStringArray
import com.nononsenseapps.feeder.db.Util.WHEREIDIS
import com.nononsenseapps.feeder.db.Util.WhereIs
import com.nononsenseapps.feeder.util.FileLog
import com.nononsenseapps.feeder.util.Optional
import com.nononsenseapps.feeder.util.getFeeds
import com.nononsenseapps.feeder.util.getIdForFeedItem
import com.nononsenseapps.feeder.util.notifyAllUris
import com.nononsenseapps.jsonfeed.Feed
import org.joda.time.DateTime
import org.joda.time.Duration
import java.io.File
import java.util.ArrayList
import java.util.Arrays

object RssLocalSync {

    /**
     * @param feedId if less than '1' then all feeds are synchronized
     * @param tag    of feeds to sync, only used if feedId is less than 1. If empty, all feeds are synced.
     */
    fun syncFeeds(context: Context, feedId: Long, tag: String) {
        val log = FileLog.getInstance(context)
        try {
            val start = DateTime.now()

            // Get all stored feeds
            val feeds: List<FeedSQL> = if (feedId > 0) {
                listFeed(context, feedId)
            } else if (!tag.isEmpty()) {
                listFeeds(context, tag)
            } else {
                listFeeds(context)
            }
            log.d(String.format("Syncing %d feeds: %s", feeds.size, start.toString()))

            val cacheDir = context.externalCacheDir

            for (f in feeds) {
                syncFeed(f, cacheDir).ifPresent { sf ->
                    val ops = convertResultToOperations(sf, f, context.contentResolver)
                    try {
                        storeSyncResults(context, ops)
                        // Notify that we've updated
                        context.contentResolver.notifyAllUris()
                    } catch (e: RemoteException) {
                        log.d("Error during sync: ${e.message}")
                        e.printStackTrace()
                    } catch (e: OperationApplicationException) {
                        log.d("Error during sync: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }

            // Finally, prune excessive items
            try {
                Cleanup.prune(context)
            } catch (e: RemoteException) {
                log.d("Error during cleanup: ${e.message}")
                e.printStackTrace()
            } catch (e: OperationApplicationException) {
                log.d("Error during cleanup: ${e.message}")
                e.printStackTrace()
            }

            val end = DateTime.now()

            val duration = Duration.millis(end.millis - start.millis)
            log.d(String.format("Finished sync after %s", duration.toString()))

            // Notify that we've updated
            context.contentResolver.notifyAllUris()
            // Send notifications for configured feeds
            notify(context)
        } catch (e: Throwable) {
            log.d("Some fatal error during sync: " + e.message)
            e.printStackTrace()
        }

    }

    private fun syncFeed(feedSQL: FeedSQL, cacheDir: File?): Optional<Feed> {
        try {
            return Optional.of(FeedParser.parseFeed(feedSQL.url, cacheDir!!))
        } catch (error: Throwable) {
            System.err.println("Error when syncing " + feedSQL.url)
            error.printStackTrace()
        }

        return Optional.empty()
    }

    @Synchronized
    @Throws(RemoteException::class, OperationApplicationException::class)
    private fun storeSyncResults(context: Context,
                                 operations: List<ContentProviderOperation>) {
        if (!operations.isEmpty()) {
            context.contentResolver
                    .applyBatch(AUTHORITY, ArrayList(operations))
        }
    }

    /**
     * @param parsedFeed
     * @param feedSQL
     * @return A list of Operations containing no back references
     */
    private fun convertResultToOperations(parsedFeed: Feed,
                                          feedSQL: FeedSQL,
                                          resolver: ContentResolver): ArrayList<ContentProviderOperation> {
        val operations = ArrayList<ContentProviderOperation>()

        val feedOp = ContentProviderOperation.newUpdate(Uri.withAppendedPath(URI_FEEDS,
                java.lang.Long.toString(feedSQL.id)))

        // This can be null, in that case do not override existing value
        val selfLink = parsedFeed.feed_url

        // Populate with values
        feedOp.withValue(COL_TITLE, parsedFeed.title)
                .withValue(COL_TAG, feedSQL.tag)
                .withValue(COL_URL, selfLink ?: feedSQL.url)

        // Add to list of operations
        operations.add(feedOp.build())

        parsedFeed.items?.forEach { entry ->
            if (entry.id != null) {
                val itemId = resolver.getIdForFeedItem(entry.id!!, feedSQL.id)
                val itemOp: ContentProviderOperation.Builder = if (itemId < 1) {
                    ContentProviderOperation.newInsert(URI_FEEDITEMS)
                } else {
                    ContentProviderOperation.newUpdate(Uri.withAppendedPath(URI_FEEDITEMS,
                            itemId.toString()))
                }

                // Use the actual id, because update operation will not return id
                itemOp.withValue(COL_FEED, feedSQL.id)

                // Be careful about nulls.
                itemOp
                        // These can be null
                        .withValue(COL_LINK, entry.url)
                        .withValue(COL_IMAGEURL, entry.image)
                        .withValue(COL_ENCLOSURELINK, entry.attachments?.firstOrNull()?.url)
                        .withValue(COL_AUTHOR, entry.author?.name)
                        .withValue(COL_PUBDATE, entry.date_published)
                        // Make sure these are non-null
                        .withValue(COL_GUID, entry.id)
                        .withValue(COL_FEEDTITLE, feedSQL.title)
                        .withValue(COL_TAG, feedSQL.tag)
                        .withValue(COL_TITLE, entry.title)
                        .withValue(COL_DESCRIPTION, entry.content_html ?: "")
                        .withValue(COL_PLAINTITLE, entry.title ?: "")
                        .withValue(COL_PLAINSNIPPET, entry.summary ?: "")

                // Add to list of operations
                operations.add(itemOp.build())
            }
        }

        return operations
    }

    private fun listFeed(context: Context, id: Long): List<FeedSQL> {
        return context.contentResolver.getFeeds(
                FEED_FIELDS.asList(), WHEREIDIS, Arrays.asList(*LongsToStringArray(id)), null)
    }

    private fun listFeeds(context: Context, tag: String): List<FeedSQL> {
        return context.contentResolver.getFeeds(
                FEED_FIELDS.asList(), WhereIs(COL_TAG), Arrays.asList(*ToStringArray(tag)), null)
    }

    private fun listFeeds(context: Context): List<FeedSQL> {
        return context.contentResolver.getFeeds(
                FEED_FIELDS.asList(), null, null, null)
    }
}
