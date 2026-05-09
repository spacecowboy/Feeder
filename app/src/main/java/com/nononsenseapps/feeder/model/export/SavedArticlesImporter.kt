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
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.system.measureTimeMillis

private const val LOG_TAG = "FEEDER_SAVEDARTIMPORT"

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
                    val links =
                        contentResolver.openInputStream(uri)?.bufferedReader()?.useLines { lines ->
                            lines
                                .map { it.trim() }
                                .filterNot { it.isBlank() }
                                .distinct()
                                .toList()
                        } ?: emptyList()

                    links.chunked(500).forEach { chunk ->
                        feedItemDao.setBookmarkedByLinks(chunk)
                    }
                }
            logDebug(LOG_TAG, "Imported saved articles in $time ms on ${Thread.currentThread().name}")
        }
    }

sealed class SavedArticlesImportError {
    abstract val throwable: Throwable?
}

data class SavedArticleImportUnknownError(
    override val throwable: Throwable,
) : SavedArticlesImportError()
