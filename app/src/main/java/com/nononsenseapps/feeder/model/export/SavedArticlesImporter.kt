package com.nononsenseapps.feeder.model.export

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItem
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.util.Either
import com.nononsenseapps.feeder.util.ToastMaker
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.system.measureTimeMillis

private const val LOG_TAG = "FEEDER_SAVEDARTIMPORT"

private val savedArticlesImportJson =
    Json {
        ignoreUnknownKeys = true
    }

suspend fun importSavedArticles(
    di: DI,
    uri: Uri,
): Either<SavedArticlesImportError, Unit> =
    Either.catching(
        onCatch = {
            Log.e(LOG_TAG, "Failed to import saved articles", it)
            val toastMaker = di.direct.instance<ToastMaker>()
            toastMaker.makeToast(R.string.failed_to_import_saved_articles)
            (it.localizedMessage ?: it.message)?.let { message ->
                toastMaker.makeToast(message)
            }

            SavedArticleImportUnknownError(it)
        },
    ) {
        withContext(Dispatchers.IO) {
            val time =
                measureTimeMillis {
                    val contentResolver: ContentResolver by di.instance()
                    val feedItemDao: FeedItemDao by di.instance()
                    val feedDao: FeedDao by di.instance()
                    val content =
                        contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                            reader.readText()
                        } ?: ""

                    if (content.trimStart().startsWith("{")) {
                        importSavedArticlesExport(
                            feedDao = feedDao,
                            feedItemDao = feedItemDao,
                            export = savedArticlesImportJson.decodeFromString(SavedArticlesExport.serializer(), content),
                        )
                    } else {
                        importLegacySavedArticleLinks(feedItemDao, content)
                    }
                }
            logDebug(LOG_TAG, "Imported saved articles in $time ms on ${Thread.currentThread().name}")
        }
    }

private suspend fun importSavedArticlesExport(
    feedDao: FeedDao,
    feedItemDao: FeedItemDao,
    export: SavedArticlesExport,
) {
    require(export.format == SAVED_ARTICLES_EXPORT_FORMAT) {
        "Unsupported saved articles export format: ${export.format}"
    }
    require(export.version == SAVED_ARTICLES_EXPORT_VERSION) {
        "Unsupported saved articles export version: ${export.version}"
    }

    export.articles.forEach { article ->
        val feedId = feedDao.resolveFeedId(article.feed)
        val existingItem =
            feedId?.let { id ->
                feedItemDao.loadFeedItem(
                    guid = article.guid.ifBlank { article.link },
                    feedId = id,
                )
            } ?: feedItemDao.loadFeedItemByLink(article.link)

        if (existingItem != null) {
            feedItemDao.setBookmarked(existingItem.id, true)
        } else {
            feedItemDao.insertFeedItem(article.toFeedItem(feedId))
        }
    }
}

private suspend fun importLegacySavedArticleLinks(
    feedItemDao: FeedItemDao,
    content: String,
) {
    val links =
        content
            .lineSequence()
            .map { it.trim() }
            .filterNot { it.isBlank() }
            .distinct()
            .toList()

    links.chunked(500).forEach { chunk ->
        feedItemDao.setBookmarkedByLinks(chunk)
    }
}

private suspend fun FeedDao.resolveFeedId(feed: SavedArticleFeedExportItem): Long? {
    val feedUrl = runCatching { URL(feed.url) }.getOrNull() ?: return null

    return getFeedIdForUrl(feedUrl)
        ?: insertFeed(
            Feed(
                title = feed.title,
                customTitle = feed.customTitle,
                url = feedUrl,
                tag = feed.tag,
                fullTextByDefault = feed.fullTextByDefault,
            ),
        )
}

private fun SavedArticleExportItem.toFeedItem(feedId: Long?): FeedItem {
    val parsedPubDate = pubDate.parseZonedDateTimeOrNull()
    val parsedPrimarySortTime =
        primarySortTime.parseInstantOrNull()
            ?: parsedPubDate?.toInstant()
            ?: Instant.now()

    return FeedItem(
        guid = guid.ifBlank { link },
        plainTitle = title,
        plainSnippet = snippet,
        thumbnailImage = thumbnailImage,
        enclosureLink = enclosureLink,
        enclosureType = enclosureType,
        author = author,
        pubDate = parsedPubDate,
        link = link,
        notified = true,
        feedId = feedId,
        firstSyncedTime = parsedPrimarySortTime,
        primarySortTime = parsedPrimarySortTime,
        bookmarked = true,
        readTime = readTime.parseInstantOrNull(),
        wordCount = wordCount,
        wordCountFull = wordCountFull,
    )
}

private fun String?.parseZonedDateTimeOrNull(): ZonedDateTime? =
    this?.let { value ->
        runCatching { ZonedDateTime.parse(value) }.getOrNull()
    }

private fun String?.parseInstantOrNull(): Instant? =
    this?.let { value ->
        runCatching { Instant.parse(value) }.getOrNull()
    }

sealed class SavedArticlesImportError {
    abstract val throwable: Throwable?
}

data class SavedArticleImportUnknownError(
    override val throwable: Throwable,
) : SavedArticlesImportError()
