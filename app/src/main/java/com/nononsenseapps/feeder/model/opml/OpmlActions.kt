package com.nononsenseapps.feeder.model.opml

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.util.ToastMaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.system.measureTimeMillis

/**
 * Exports OPML on a background thread
 */
suspend fun exportOpml(di: DI, uri: Uri) = withContext(Dispatchers.IO) {
    try {
        val time = measureTimeMillis {
            val contentResolver: ContentResolver by di.instance()
            val feedDao: FeedDao by di.instance()
            contentResolver.openOutputStream(uri)?.let {
                writeOutputStream(
                    it,
                    feedDao.loadTags()
                ) { tag ->
                    feedDao.loadFeeds(tag = tag)
                }
            }
        }
        Log.d("OPML", "Exported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e("OMPL", "Failed to export OMPL", e)
        di.direct.instance<ToastMaker>().makeToast("Failed to export OMPL")
    }
}

/**
 * Imports OPML on a background thread
 */
suspend fun importOpml(di: DI, uri: Uri) = withContext(Dispatchers.IO) {
    val db: AppDatabase by di.instance()
    try {
        val time = measureTimeMillis {
            val parser = OpmlParser(OPMLToRoom(db))
            val contentResolver: ContentResolver by di.instance()
            contentResolver.openInputStream(uri).use {
                it?.let { stream ->
                    parser.parseInputStream(stream)
                }
            }
            requestFeedSync(di = di, ignoreConnectivitySettings = false, parallell = true)
        }
        Log.d("OPML", "Imported OPML in $time ms on ${Thread.currentThread().name}")
    } catch (e: Throwable) {
        Log.e("OMPL", "Failed to import OMPL", e)
        di.direct.instance<ToastMaker>().makeToast("Failed to import OMPL")
    }
}
