package com.nononsenseapps.feeder.model

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.blob.blobFullOutputStream
import com.nononsenseapps.feeder.db.room.FeedItemForFetching
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.di.CURRENTLY_SYNCING_STATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.withContext
import net.dankito.readability4j.Readability4J
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.io.File
import java.net.URL

const val ARG_FEED_ITEM_ID = "feed_item_id"
const val ARG_FEED_ITEM_LINK = "feed_item_link"

fun scheduleFullTextParse(
    di: DI,
    feedItem: FeedItemForFetching
) {
    val workRequest = OneTimeWorkRequestBuilder<FullTextWorker>()

    val data = workDataOf(
        ARG_FEED_ITEM_ID to feedItem.id,
        ARG_FEED_ITEM_LINK to feedItem.link
    )

    workRequest.setInputData(data)
    val workManager by di.instance<WorkManager>()
    workManager.enqueue(workRequest.build())
}

class FullTextWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), DIAware {
    override val di: DI by closestDI(context)
    private val currentlySyncing: ConflatedBroadcastChannel<Boolean> by instance(tag = CURRENTLY_SYNCING_STATE)
    private val okHttpClient: OkHttpClient by instance()

    override suspend fun doWork(): Result {
        val ignoreConnectivitySettings = inputData.getBoolean(IGNORE_CONNECTIVITY_SETTINGS, false)
        var success = false

        if (ignoreConnectivitySettings || isOkToSyncAutomatically(context)) {
            val feedItemId: Long = inputData.getLong(ARG_FEED_ITEM_ID, ID_UNSET)
            val link: String? = inputData.getString(ARG_FEED_ITEM_LINK)
                ?: throw RuntimeException("No link provided")

            if (!currentlySyncing.isClosedForSend) {
                currentlySyncing.offer(true)
            }

            Log.i("FeederFullText", "Worker going to parse $feedItemId: $link")

            success = parseFullArticleIfMissing(
                feedItem = object : FeedItemForFetching {
                    override val id = feedItemId
                    override val link = link
                },
                okHttpClient = okHttpClient,
                filesDir = context.filesDir
            )

            if (!currentlySyncing.isClosedForSend) {
                currentlySyncing.offer(false)
            }
        }

        return when (success) {
            true -> Result.success()
            false -> Result.failure()
        }
    }
}

suspend fun parseFullArticleIfMissing(
    feedItem: FeedItemForFetching,
    okHttpClient: OkHttpClient,
    filesDir: File
): Boolean {
    val fullArticleFile = blobFullFile(itemId = feedItem.id, filesDir = filesDir)
    return fullArticleFile.isFile || parseFullArticle(
        feedItem = feedItem,
        okHttpClient = okHttpClient,
        filesDir = filesDir
    ).first
}

suspend fun parseFullArticle(
    feedItem: FeedItemForFetching,
    okHttpClient: OkHttpClient,
    filesDir: File
): Pair<Boolean, Throwable?> = withContext(Dispatchers.Default) {
    return@withContext try {
        val url = feedItem.link ?: return@withContext false to null
        Log.i("FeederFullText", "Fetching full page ${feedItem.link}")
        val html: String = okHttpClient.curl(URL(url)) ?: return@withContext false to null

        // TODO verify encoding is respected in reader
        Log.i("FeederFullText", "Parsing article ${feedItem.link}")
        val article = Readability4J(url, html).parse()

        // TODO set image on item if none already
        // naiveFindImageLink(article.content)?.let { Parser.unescapeEntities(it, true) }

        Log.i("FeederFullText", "Writing article ${feedItem.link}")
        withContext(Dispatchers.IO) {
            blobFullOutputStream(feedItem.id, filesDir).bufferedWriter().use { writer ->
                writer.write(article.contentWithUtf8Encoding)
            }
        }
        true to null
    } catch (e: Throwable) {
        Log.e(
            "FeederFullText",
            "Failed to get fulltext for ${feedItem.link}: ${e.message}",
            e
        )
        false to e
    }
}
