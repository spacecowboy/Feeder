package com.nononsenseapps.feeder.model

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.Feed

class FeedViewModel(application: Application, private val feedId: Long) : AndroidViewModel(application) {
    val dao = AppDatabase.getInstance(application).feedDao()
    val liveFeed: LiveData<Feed> = dao.loadLiveFeed(feedId)

    fun deleteFeed() {
        liveFeed.value?.let {
            dao.deleteFeed(it)
        }
    }
}

class FeedViewModelFactory(private val application: Application,
                           private val feedId: Long) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FeedViewModel(application = application, feedId = feedId) as T
    }
}

fun Fragment.getFeedViewModel(feedId: Long): FeedViewModel {
    val factory = FeedViewModelFactory(activity!!.application, feedId)
    return ViewModelProviders.of(this, factory).get(FeedViewModel::class.java)
}
