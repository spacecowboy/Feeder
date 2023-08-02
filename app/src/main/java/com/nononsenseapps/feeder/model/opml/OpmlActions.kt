package com.nononsenseapps.feeder.model.opml

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.SettingsStore
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.model.OPMLParserHandler
import com.nononsenseapps.feeder.model.workmanager.requestFeedSync
import com.nononsenseapps.feeder.util.ToastMaker
import com.nononsenseapps.feeder.util.logDebug
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

private const val LOG_TAG = "FEEDER_OPMLACTIONS"

/**
 * Exports OPML on a background thread
 */
suspend fun exportOpml(di: DI, uri: Uri) = withContext(Dispatchers.IO) {
    try {
        val time = measureTimeMillis {
            val contentResolver: ContentResolver by di.instance()
            val feedDao: FeedDao by di.instance()
            val settingsStore: SettingsStore by di.instance()
            contentResolver.openOutputStream(uri)?.let {
                writeOutputStream(
                    os = it,
                    settings = settingsStore.getAllSettings(),
                    blockedPatterns = settingsStore.blockListPreference.first(),
                    tags = feedDao.loadTags(),
                ) { tag ->
                    feedDao.loadFeeds(tag = tag)
                }
            }
        }
        logDebug(LOG_TAG, "Exported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e(LOG_TAG, "Failed to export OPML", e)
        val toastMaker = di.direct.instance<ToastMaker>()
        toastMaker.makeToast(R.string.failed_to_export_OPML)
        (e.localizedMessage ?: e.message)?.let { message ->
            toastMaker.makeToast(message)
        }
    }
}

/**
 * Imports OPML on a background thread
 */
suspend fun importOpml(di: DI, uri: Uri) = withContext(Dispatchers.IO) {
    val opmlToRoom: OPMLParserHandler by di.instance()
    try {
        val time = measureTimeMillis {
            val parser = OpmlPullParser(opmlToRoom)
            val contentResolver: ContentResolver by di.instance()
            contentResolver.openInputStream(uri).use {
                it?.let { stream ->
                    parser.parseInputStream(stream)
                }
            }
            requestFeedSync(di = di)
        }
        logDebug(LOG_TAG, "Imported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e(LOG_TAG, "Failed to import OPML", e)
        val toastMaker = di.direct.instance<ToastMaker>()
        toastMaker.makeToast(R.string.failed_to_import_OPML)
        (e.localizedMessage ?: e.message)?.let { message ->
            toastMaker.makeToast(message)
        }
    }
}
