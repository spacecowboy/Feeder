package com.nononsenseapps.feeder.model

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.blob.blobFullOutputStream
import com.nononsenseapps.feeder.db.room.FeedItemForFetching
import com.nononsenseapps.feeder.model.FullTextParser.Companion.LOG_TAG
import com.nononsenseapps.feeder.util.FilePathProvider
import com.nononsenseapps.feeder.util.logDebug
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import net.dankito.readability4j.extended.Readability4JExtended
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

fun scheduleFullTextParse(
    di: DI,
) {
    Log.i(LOG_TAG, "Scheduling a full text parse work")
    val workRequest = OneTimeWorkRequestBuilder<FullTextWorker>()
        .addTag("feeder")
        .keepResultsForAtLeast(1, TimeUnit.MINUTES)

    val workManager by di.instance<WorkManager>()
    workManager.enqueue(workRequest.build())
}

class FullTextWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), DIAware {
    override val di: DI by closestDI(context)
    private val fullTextParser: FullTextParser by instance()

    override suspend fun doWork(): Result {
        Log.i(LOG_TAG, "Performing full text parse work")
        return when (fullTextParser.parseFullArticlesForMissing()) {
            true -> Result.success()
            false -> Result.failure()
        }.also {
            Log.i(LOG_TAG, "Finished full text parse work")
        }
    }
}

class FullTextParser(override val di: DI) : DIAware {
    private val repository: Repository by instance()
    private val okHttpClient: OkHttpClient by instance()
    private val filePathProvider: FilePathProvider by instance()

    suspend fun parseFullArticlesForMissing(): Boolean {
        logDebug(LOG_TAG, "Parsing full texts for missing")
        val itemsToSync: List<FeedItemForFetching> =
            repository.getFeedsItemsWithDefaultFullTextNeedingDownload()
                .firstOrNull()
                ?: emptyList()

        return itemsToSync
            .map { feedItem ->
                parseFullArticleIfMissing(
                    feedItem = feedItem,
                )
            }
            .fold(true) { acc, value ->
                acc && value
            }
    }

    suspend fun parseFullArticleIfMissing(feedItem: FeedItemForFetching): Boolean {
        val fullArticleFile =
            blobFullFile(itemId = feedItem.id, filesDir = filePathProvider.fullArticleDir)
        if (fullArticleFile.isFile) {
            return true
        }

        return try {
            parseFullArticle(feedItem = feedItem).first
        } finally {
            repository.markAsFullTextDownloaded(feedItem.id)
        }
    }

    private suspend fun parseFullArticle(feedItem: FeedItemForFetching): Pair<Boolean, Throwable?> =
        withContext(Dispatchers.Default) {
            return@withContext try {
                val url = feedItem.link ?: return@withContext false to null
                logDebug(LOG_TAG, "Fetching full page ${feedItem.link}")
                val html: String = okHttpClient.curl(URL(url)) ?: return@withContext false to null

                // TODO verify encoding is respected in reader
                Log.i(LOG_TAG, "Parsing article ${feedItem.link}")
                val article = Readability4JExtended(url, html).parse()

                // TODO set image on item if none already
                // naiveFindImageLink(article.content)?.let { Parser.unescapeEntities(it, true) }

                logDebug(LOG_TAG, "Writing article ${feedItem.link}")
                withContext(Dispatchers.IO) {
                    filePathProvider.fullArticleDir.mkdirs()
                    blobFullOutputStream(feedItem.id, filePathProvider.fullArticleDir)
                        .bufferedWriter().use { writer ->
                            writer.write(article.contentWithUtf8Encoding)
                        }
                }
                true to null
            } catch (e: Throwable) {
                Log.e(
                    LOG_TAG,
                    "Failed to get fulltext for ${feedItem.link}: ${e.message}",
                    e
                )
                false to e
            }
        }

    companion object {
        internal const val LOG_TAG = "FEEDER_FULLTEXT"
    }
}
