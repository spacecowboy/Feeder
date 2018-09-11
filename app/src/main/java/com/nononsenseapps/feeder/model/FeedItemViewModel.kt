package com.nononsenseapps.feeder.model

import android.app.Activity
import android.app.Application
import android.graphics.Point
import android.text.Spanned
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.BackgroundUI
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.ui.text.toSpannedWithImages
import com.nononsenseapps.feeder.ui.text.toSpannedWithNoImages
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.TabletUtils
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class FeedItemViewModel(application: Application, id: Long, maxImageSize: Point) : AndroidViewModel(application) {
    val dao = AppDatabase.getInstance(application).feedItemDao()

    val liveItem: LiveData<FeedItemWithFeed> = dao.loadLiveFeedItem(id)

    val liveImageText: MediatorLiveData<Spanned> = MediatorLiveData()

    init {
        liveImageText.addSource(liveItem) {
            it?.let {
                liveImageText.value = toSpannedWithNoImages(application, it.description, it.feedUrl)
                launch(BackgroundUI) {
                    val allowDownload = PrefUtils.shouldLoadImages(application)
                    val spanned = toSpannedWithImages(application, it.description, it.feedUrl, maxImageSize, allowDownload)
                    withContext(UI) {
                        liveImageText.value = spanned
                    }
                }
            }
        }
    }
}

class FeedItemViewModelFactory(private val application: Application,
                               private val id: Long,
                               private val maxImageSize: Point) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FeedItemViewModel(application = application, id = id, maxImageSize = maxImageSize) as T
    }
}

fun Fragment.getFeedItemViewModel(id: Long): FeedItemViewModel {
    val factory = FeedItemViewModelFactory(activity!!.application, id, activity!!.maxImageSize())
    return ViewModelProviders.of(this, factory).get(FeedItemViewModel::class.java)
}

private fun Activity.maxImageSize(): Point {
    val size = Point()
    windowManager?.defaultDisplay?.getSize(size)
    if (TabletUtils.isTablet(this)) {
        // Using twice window height since we do scroll vertically
        size.set(Math.round(resources.getDimension(R.dimen.reader_tablet_width)), 2 * size.y)
    } else {
        // Base it on window size
        size.set(size.x - 2 * Math.round(resources.getDimension(R.dimen.keyline_1)), 2 * size.y)
    }

    return size
}
