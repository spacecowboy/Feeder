package com.nononsenseapps.feeder.model.export

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.util.Either
import com.nononsenseapps.feeder.util.ToastMaker
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.system.measureTimeMillis

private const val LOG_TAG = "FEEDER_SAVEDARTEXPORT"
const val SAVED_ARTICLES_EXPORT_FORMAT = "feeder-saved-articles"
const val SAVED_ARTICLES_EXPORT_VERSION = 1

private val savedArticlesJson =
    Json {
        prettyPrint = true
    }

suspend fun exportSavedArticles(
    di: DI,
    uri: Uri,
): Either<SavedArticlesExportError, Unit> =
    Either.catching(
        onCatch = {
            Log.e(LOG_TAG, "Failed to export saved articles", it)
            val toastMaker = di.direct.instance<ToastMaker>()
            toastMaker.makeToast(R.string.failed_to_export_saved_articles)
            (it.localizedMessage ?: it.message)?.let { message ->
                toastMaker.makeToast(message)
            }

            SavedArticleExportUnknownError(it)
        },
    ) {
        withContext(Dispatchers.IO) {
            val time =
                measureTimeMillis {
                    val contentResolver: ContentResolver by di.instance()
                    val feedItemDao: FeedItemDao by di.instance()
                    contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { bw ->
                        val export =
                            SavedArticlesExport(
                                format = SAVED_ARTICLES_EXPORT_FORMAT,
                                version = SAVED_ARTICLES_EXPORT_VERSION,
                                articles =
                                    feedItemDao
                                        .getLinksOfBookmarks()
                                        .map(::SavedArticleExportItem),
                            )
                        bw.write(savedArticlesJson.encodeToString(export))
                    }
                }
            logDebug(LOG_TAG, "Exported saved articles in $time ms on ${Thread.currentThread().name}")
        }
    }

@Serializable
data class SavedArticlesExport(
    val format: String,
    val version: Int,
    val articles: List<SavedArticleExportItem>,
)

@Serializable
data class SavedArticleExportItem(
    val link: String,
)

sealed class SavedArticlesExportError {
    abstract val throwable: Throwable?
}

data class SavedArticleExportUnknownError(
    override val throwable: Throwable,
) : SavedArticlesExportError()
