package com.nononsenseapps.feeder.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_BROWSER
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_CUSTOM_TAB
import com.nononsenseapps.feeder.util.PREF_VAL_OPEN_WITH_READER
import com.nononsenseapps.feeder.util.PrefValOpenWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.instance

private const val KEY_ITEM_ID = "FeedItemViewModel itemid"

class FeedItemViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val dao: FeedItemDao by instance()
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()
    val context: Application by instance()
    private val okHttpClient: OkHttpClient by instance()

    var currentItemId: Long
        get() = state[KEY_ITEM_ID] ?: ID_UNSET
        set(value) = state.set(KEY_ITEM_ID, value)

    suspend fun getOpenArticleWith(itemId: Long): PrefValOpenWith =
        when (dao.getOpenArticleWith(itemId)) {
            PREF_VAL_OPEN_WITH_BROWSER -> PrefValOpenWith.OPEN_WITH_BROWSER
            PREF_VAL_OPEN_WITH_CUSTOM_TAB -> PrefValOpenWith.OPEN_WITH_CUSTOM_TAB
            PREF_VAL_OPEN_WITH_READER -> PrefValOpenWith.OPEN_WITH_READER
            else -> PrefValOpenWith.OPEN_WITH_DEFAULT
        }

    suspend fun getLink(itemId: Long): String? =
        dao.getLink(itemId)

    private val textToDisplayState = MutableStateFlow(TextToDisplay.DEFAULT)
    val textToDisplay: StateFlow<TextToDisplay> = textToDisplayState.asStateFlow()
    fun displayArticleText() {
        textToDisplayState.value = TextToDisplay.DEFAULT
    }

    fun displayFullText() {
        if (blobFullFile(currentItemId, context.filesDir).isFile) {
            textToDisplayState.value = TextToDisplay.FULLTEXT
            return
        }

        textToDisplayState.value = TextToDisplay.LOADING_FULLTEXT

        viewModelScope.launch {
            val item = dao.loadFeedItem(currentItemId)

            if (item == null) {
                Log.e("FeederItemViewModel", "No such item: $currentItemId")
                textToDisplayState.value = TextToDisplay.FAILED_TO_LOAD_FULLTEXT
            } else {
                val (result, throwable) = parseFullArticle(
                    item,
                    okHttpClient,
                    context.filesDir
                )

                if (!result) {
                    textToDisplayState.value = TextToDisplay.FAILED_TO_LOAD_FULLTEXT
                    Log.e("FeederItemViewModel", "Failed to load full text", throwable)
                } else {
                    textToDisplayState.value = TextToDisplay.FULLTEXT
                }
            }
        }
    }

    private val currentLiveItemId: LiveData<Long> =
        state.getLiveData(KEY_ITEM_ID, ID_UNSET)

    val currentLiveItem: LiveData<FeedItemWithFeed?> =
        currentLiveItemId.switchMap { itemId ->
            dao.loadFeedItemFlow(itemId).asLiveData()
        }

    fun markCurrentItemAsUnread() {
        applicationCoroutineScope.launch {
            dao.markAsRead(id = currentItemId, unread = true)
        }
    }

    suspend fun markAsRead(id: Long, unread: Boolean = false) =
        dao.markAsRead(id = id, unread = unread)

    suspend fun markAsReadAndNotified(id: Long) = dao.markAsReadAndNotified(id = id)
}

enum class TextToDisplay {
    DEFAULT,
    LOADING_FULLTEXT,
    FAILED_TO_LOAD_FULLTEXT,
    FULLTEXT,
}
