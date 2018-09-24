package com.nononsenseapps.feeder.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import java.io.FileDescriptor
import java.io.PrintWriter
import java.util.*

typealias ResultMap<T> = Map<T, Int>
typealias MutableResultMap<T> = MutableMap<T, Int>

/**
 * Static library support version of the framework's [android.content.CursorLoader].
 * Used to write apps that run on platforms prior to Android 3.0.  When running
 * on Android 3.0 or above, this implementation is still used; it does not try
 * to switch to the framework's implementation.  See the framework SDK
 * documentation for a class overview.
 */
abstract class DeltaCursorLoader<T: Any>(context: Context,
                                         val uri: Uri,
                                         val projection: Array<String>,
                                         val selection: String?,
                                         val selectionArgs: Array<String>?,
                                         val sortOrder: String?) : androidx.loader.content.AsyncTaskLoader<ResultMap<T>?>(context) {
    // This will call ForceLoad() when data changes
    internal val observer: ForceLoadContentObserver = ForceLoadContentObserver()

    internal var cursor: Cursor? = null

    private var lastResult: Map<T, Int>? = null

    /* Runs on a worker thread */
    abstract override fun loadInBackground(): ResultMap<T>?


    /* Runs on the UI thread */
    override fun deliverResult(result: Map<T, Int>?) {
        if (isReset) {
            // An async query came in while the loader is stopped
            //            if (cursor != null) {
            //                cursor.close();
            //            }
            return
        }

        lastResult = result
        if (isStarted) {
            Log.d(TAG, "Delivering result")
            super.deliverResult(result)
        } else {
            Log.d(TAG, "Not started, so not delivering")
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     *
     *
     * Must be called from the UI thread
     */
    override fun onStartLoading() {
        if (lastResult != null) {
            deliverResult(lastResult)
        }
        if (takeContentChanged() || lastResult == null) {
            forceLoad()
        }
    }

    /**
     * Must be called from the UI thread
     */
    override fun onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad()
    }

    override fun onCanceled(map: Map<T, Int>?) {
        if (cursor != null && !cursor!!.isClosed) {
            cursor!!.close()
        }
    }

    override fun onReset() {
        super.onReset()

        // Ensure the loader is stopped
        onStopLoading()

        if (cursor != null && !cursor!!.isClosed) {
            cursor!!.close()
        }
        cursor = null
        lastResult = null
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun dump(prefix: String, fd: FileDescriptor?, writer: PrintWriter, args: Array<String>?) {
        super.dump(prefix, fd, writer, args)
        writer.print(prefix)
        writer.print("mUri=")
        writer.println(uri)
        writer.print(prefix)
        writer.print("mProjection=")
        writer.println(Arrays.toString(projection))
        writer.print(prefix)
        writer.print("mSelection=")
        writer.println(selection)
        writer.print(prefix)
        writer.print("mSelectionArgs=")
        writer.println(Arrays.toString(selectionArgs))
        writer.print(prefix)
        writer.print("mSortOrder=")
        writer.println(sortOrder)
        writer.print(prefix)
        writer.print("cursor=")
        writer.println(cursor)
        //writer.print(prefix); writer.print("mContentChanged="); writer.println(mContentChanged);
    }

    companion object {
        private val TAG = "DeltaCursorLoader"
    }
}
