package com.nononsenseapps.feeder.model

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.nononsenseapps.feeder.coroutines.BackgroundUI
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class FeedItemsViewModel(application: Application, val feedId: Long, val tag: String, onlyUnread: Boolean = true) : AndroidViewModel(application) {

    private val dao: FeedItemDao = AppDatabase.getInstance(application).feedItemDao()
    private val liveOnlyUnread = MutableLiveData<Boolean>()
    private val liveDbPreviews: LiveData<List<PreviewItem>> = Transformations.switchMap(liveOnlyUnread) { onlyUnread ->
        when {
            feedId > ID_UNSET -> if (onlyUnread) dao.loadLiveUnreadPreviews(feedId = feedId) else dao.loadLivePreviews(feedId = feedId)
            feedId == ID_ALL_FEEDS -> if (onlyUnread) dao.loadLiveUnreadPreviews() else dao.loadLivePreviews()
            tag.isNotEmpty() -> if (onlyUnread) dao.loadLiveUnreadPreviews(tag = tag) else dao.loadLivePreviews(tag = tag)
            else -> throw IllegalArgumentException("Tag was empty, but no valid feed id was provided either")
        }
    }

    // Used to update view directly
    private val tempItems = MutableLiveData<List<PreviewItem>>()

    val livePreviews: MediatorLiveData<List<PreviewItem>> = MediatorLiveData()

    init {
        liveOnlyUnread.value = onlyUnread
        livePreviews.addSource(liveDbPreviews) {
            livePreviews.value = it
        }
        livePreviews.addSource(tempItems) {
            livePreviews.value = it
        }
    }

    fun setOnlyUnread(onlyUnread: Boolean) {
        liveOnlyUnread.value = onlyUnread
    }

    fun markAllAsRead() {
        launch(BackgroundUI) {
            if (liveOnlyUnread.value == true) {
                tempItems.postValue(emptyList())
            } else {
                tempItems.postValue(livePreviews.value?.map {
                    if (it.unread) {
                        it.copy(unread = false)
                    } else {
                        it
                    }
                })
            }

            when {
                feedId > ID_UNSET -> dao.markAllAsRead(feedId)
                feedId == ID_ALL_FEEDS -> dao.markAllAsRead()
                tag.isNotEmpty() -> dao.markAllAsRead(tag)
            }
        }
    }

    fun toggleReadState(feedItem: PreviewItem) {
        launch(BackgroundUI) {
            val toggledItem = feedItem.copy(unread = !feedItem.unread)
            if (liveOnlyUnread.value == true) {
                tempItems.postValue(livePreviews.value?.filter { it.id != feedItem.id })
            } else {
                tempItems.postValue(livePreviews.value?.map {
                    if (it.id == feedItem.id) {
                        toggledItem
                    } else {
                        it
                    }
                })
            }

            dao.markAsRead(toggledItem.id, unread = toggledItem.unread)
            cancelNotificationInBackground(getApplication(), feedItem.id)
        }
    }

    /**
     * Already called within a coroutine scope, do not launch another to keep ordering guarantees
     */
    suspend fun markAsNotified() {
        withContext(BackgroundUI) {
            when {
                feedId > ID_UNSET -> dao.markAsNotified(feedId)
                feedId == ID_ALL_FEEDS -> dao.markAllAsNotified()
                tag.isNotEmpty() -> dao.markTagAsNotified(tag)
            }
        }
    }
}

class FeedItemsViewModelFactory(private val application: Application,
                                private val feedId: Long,
                                private val tag: String,
                                private val onlyUnread: Boolean) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FeedItemsViewModel(application = application, feedId = feedId, tag = tag, onlyUnread = onlyUnread) as T
    }
}

fun Fragment.getFeedItemsViewModel(onlyUnread: Boolean = true, feedId: Long = ID_UNSET, tag: String = ""): FeedItemsViewModel {
    val factory = FeedItemsViewModelFactory(activity!!.application,
            onlyUnread = onlyUnread,
            feedId = feedId,
            tag = tag)
    return ViewModelProviders.of(this, factory).get(FeedItemsViewModel::class.java)
}
