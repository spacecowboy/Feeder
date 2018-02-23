package com.nononsenseapps.feeder.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.os.OperationCanceledException


class ConvertedCursorLoader<T>(context: Context,
                               val uri: Uri,
                               val projection: Array<String>,
                               val selection: String? = null,
                               val selectionArgs: Array<String>? = null,
                               val sortOrder: String? = null,
                               val cursorConverter: ((Cursor?) -> T?)) : AsyncTaskLoader<Result<T>>(context) {
    private val observer: ForceLoadContentObserver = ForceLoadContentObserver()
    private var lastResult: Result<T>? = null
    private var cancellationSignal: CancellationSignal? = null

    override fun loadInBackground(): Result<T>? {
        synchronized(this) {
            if (isLoadInBackgroundCanceled) {
                throw OperationCanceledException()
            }
            cancellationSignal = CancellationSignal()
        }
        try {
            val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal)
            if (cursor != null) {
                try {
                    // Ensure the cursor window is filled.
                    cursor.count
                    cursor.registerContentObserver(observer)
                } catch (ex: RuntimeException) {
                    cursor.close()
                    throw ex
                }

            }

            try {
                return Result(cursor, cursorConverter(cursor))
            } catch (ex: RuntimeException) {
                cursor.close()
                throw ex
            }
        } finally {
            synchronized(this) {
                cancellationSignal = null
            }
        }
    }

    override fun cancelLoadInBackground() {
        super.cancelLoadInBackground()

        synchronized(this) {
            cancellationSignal?.cancel()
        }
    }

    override fun deliverResult(result: Result<T>?) {
        if (isReset) {
            // An async query came in while the loader is stopped
            result?.cursor?.close()
            return
        }
        val oldCursor = lastResult?.cursor
        lastResult = result

        if (isStarted) {
            super.deliverResult(result)
        }

        if (oldCursor !== result?.cursor && oldCursor?.isClosed == false) {
            oldCursor.close()
        }
    }

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

    override fun onCanceled(result: Result<T>?) {
        if (result?.cursor?.isClosed == false) {
            result.cursor.close()
        }
    }


    override fun onReset() {
        super.onReset()

        // Ensure the loader is stopped
        onStopLoading()

        if (lastResult?.cursor?.isClosed == false) {
            lastResult?.cursor?.close()
        }
        lastResult = null
    }
}

/**
 * Cursor is used internally for cancellation purposes
 */
class Result<out T>(internal val cursor: Cursor?, val data: T?)
