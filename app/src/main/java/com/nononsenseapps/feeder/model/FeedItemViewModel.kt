package com.nononsenseapps.feeder.model

import android.app.Activity
import android.app.Application
import android.graphics.Point
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.KodeinAwareViewModel
import com.nononsenseapps.feeder.blob.blobInputStream
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.ui.text.UrlClickListener
import com.nononsenseapps.feeder.ui.text.toSpannedWithImages
import com.nononsenseapps.feeder.ui.text.toSpannedWithNoImages
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.TabletUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.io.IOException
import java.net.URL
import kotlin.math.roundToInt

@FlowPreview
class FeedItemViewModel(kodein: Kodein) : KodeinAwareViewModel(kodein) {
    private val dao: FeedItemDao by instance()
    private val prefs: Prefs by instance()
    val context: Application by kodein.instance()

    private lateinit var liveItem: LiveData<FeedItemWithFeed> /*by lazy { dao.loadLiveFeedItem(id) }*/

    fun getLiveItem(id: Long): LiveData<FeedItemWithFeed> {
        if (!this::liveItem.isInitialized) {
            liveItem = dao.loadLiveFeedItem(id).asLiveData()
        }
        return liveItem
    }

    private lateinit var liveImageText: LiveData<Spanned>

    fun getLiveImageText(
            id: Long,
            maxImageSize: Point,
            urlClickListener: UrlClickListener?
    ): LiveData<Spanned> {
        if (!this::liveImageText.isInitialized) {
            liveImageText = liveData(context = viewModelScope.coroutineContext) {
                // TODO resources
                emit(SpannableString("Loading..."))

                try {
                    withContext(Dispatchers.IO) {
                        val allowDownload = prefs.shouldLoadImages()
                        val feedUrl = dao.loadFeedUrlOfFeedItem(id = id)
                                ?: URL("https://missing.feedurl")

                        val noImages = blobInputStream(
                                itemId = id,
                                filesDir = context.filesDir
                        ).bufferedReader().use { reader ->
                            toSpannedWithNoImages(
                                    kodein = kodein,
                                    source = reader,
                                    siteUrl = feedUrl,
                                    maxSize = maxImageSize,
                                    urlClickListener = urlClickListener
                            )
                        }
                        emit(noImages)

                        if (noImages.getAllImageSpans().isNotEmpty()) {
                            val withImages = blobInputStream(
                                    itemId = id,
                                    filesDir = context.filesDir
                            ).bufferedReader().use { reader ->
                                toSpannedWithImages(
                                        kodein = kodein,
                                        source = reader,
                                        siteUrl = feedUrl,
                                        maxSize = maxImageSize,
                                        allowDownload = allowDownload,
                                        urlClickListener = urlClickListener
                                )
                            }
                            emit(withImages)
                        }
                    }
                } catch (e: IOException) {
                    // TODO resources
                    emit(SpannableString("Could not read blob for item with id [$id]"))
                }
            }
        }

        return liveImageText
    }

    suspend fun markAsRead(id: Long, unread: Boolean = false) = dao.markAsRead(id = id, unread = unread)
    suspend fun markAsReadAndNotified(id: Long) = dao.markAsReadAndNotified(id = id)
}

internal fun Activity.maxImageSize(): Point {
    val size = Point()
    windowManager?.defaultDisplay?.getSize(size)
    if (TabletUtils.isTablet(this)) {
        // Using twice window height since we do scroll vertically
        size.set(resources.getDimension(R.dimen.reader_tablet_width).roundToInt(), 2 * size.y)
    } else {
        // Base it on window size
        size.set(size.x - 2 * resources.getDimension(R.dimen.keyline_1).roundToInt(), 2 * size.y)
    }

    return size
}

private fun Spanned.getAllImageSpans(): Array<out ImageSpan> =
        getSpans(0, length, ImageSpan::class.java) ?: emptyArray()
