package com.nononsenseapps.feeder.ui.compose.editfeed

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_BROWSER
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_CUSTOM_TAB
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_READER
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_WEBVIEW
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.model.requestFeedSync
import java.net.URL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class EditFeedScreenViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val repository: Repository by instance()

    val feedId: Long = state["feedId"]
        ?: throw IllegalArgumentException("Missing feedId in savedState")

    private val _url = state.getLiveData("feedUrl", "")
    fun setUrl(value: String) {
        state["feedUrl"] = value
    }

    private val _tag = state.getLiveData("feedTag", "")
    fun setTag(value: String) {
        state["feedTag"] = value
    }

    private val _title = state.getLiveData("feedTitle", "")
    fun setTitle(value: String) {
        state["feedTitle"] = value
    }

    private val _fullTextByDefault = state.getLiveData("fullTextByDefault", false)
    fun setFullTextByDefault(value: Boolean) {
        state["fullTextByDefault"] = value
    }

    private val _notify = state.getLiveData("notify", false)
    fun setNotify(value: Boolean) {
        state["notify"] = value
    }

    private val _articleOpener = state.getLiveData("articleOpener", PREF_VAL_OPEN_WITH_READER)
    fun setArticleOpener(value: String) {
        state["articleOpener"] = value
    }

    private val _alternateId = state.getLiveData("alternateId", false)
    fun setAlternateId(value: Boolean) {
        state["alternateId"] = value
    }

    fun saveInBackgroundAndRequestSync() {
        val url = _url.value
            ?: error("Missing url in state!!!")

        viewModelScope.launch {
            val feed = repository.getFeed(feedId)
                ?: Feed() // Feed was deleted while being edited?
            val savedId = repository.saveFeed(
                feed.copy(
                    url = URL(url),
                    title = _title.value ?: "",
                    customTitle = _title.value ?: "",
                    tag = _tag.value ?: "",
                    fullTextByDefault = _fullTextByDefault.value ?: false,
                    notify = _notify.value ?: false,
                    openArticlesWith = _articleOpener.value ?: PREF_VAL_OPEN_WITH_READER,
                    alternateId = _alternateId.value ?: false,
                )
            )
            requestFeedSync(
                di,
                feedId = savedId,
                forceNetwork = true,
            )
        }
    }

    private val feedDefaultTitleFlow: Flow<String> = flow {
        emit(
            repository.getFeed(feedId)?.title ?: ""
        )
    }

    private val _viewState = MutableStateFlow(EditFeedViewState())
    val viewState: StateFlow<EditFeedViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            // Set initial state in case state is empty
            val feed = repository.getFeed(feedId)
                ?: throw IllegalArgumentException("No feed with id $feedId!")

            // Is this actually necessary anymore?
            if (!state.contains("feedUrl")) {
                setUrl(feed.url.toString())
            }
            if (!state.contains("feedTag")) {
                setTag(feed.tag)
            }
            if (!state.contains("feedTitle")) {
                setTitle(feed.displayTitle)
            }
            if (!state.contains("fullTextByDefault")) {
                setFullTextByDefault(feed.fullTextByDefault)
            }
            if (!state.contains("notify")) {
                setNotify(feed.notify)
            }
            if (!state.contains("articleOpener")) {
                setArticleOpener(feed.openArticlesWith)
            }
            if (!state.contains("alternateId")) {
                setAlternateId(feed.alternateId)
            }

            combine(
                repository.allTags,
                _tag.asFlow(),
                _url.asFlow(),
                _title.asFlow(),
                _fullTextByDefault.asFlow(),
                _notify.asFlow(),
                _articleOpener.asFlow(),
                feedDefaultTitleFlow,
                _alternateId.asFlow(),
            ) { params: Array<Any> ->
                @Suppress("UNCHECKED_CAST")
                EditFeedViewState(
                    allTags = params[0] as List<String>,
                    tag = params[1] as String,
                    url = params[2] as String,
                    title = params[3] as String,
                    fullTextByDefault = params[4] as Boolean,
                    notify = params[5] as Boolean,
                    articleOpener = params[6] as String,
                    defaultTitle = params[7] as String,
                    alternateId = params[8] as Boolean,
                )
            }.collect {
                _viewState.value = it
            }
        }
    }
}

// All fields maintained in savedState
// Viewmodel will survive rotation - so have to test with memory kill app
@Immutable
data class EditFeedViewState(
    val allTags: List<String> = emptyList(),
    val tag: String = "",
    val url: String = "",
    val title: String = "",
    val fullTextByDefault: Boolean = false,
    val notify: Boolean = false,
    val articleOpener: String = "",
    val defaultTitle: String = "",
    val alternateId: Boolean = false,
) {
    val isOpenItemWithBrowser: Boolean
        get() = articleOpener==PREF_VAL_OPEN_WITH_BROWSER

    val isOpenItemWithCustomTab: Boolean
        get() = articleOpener==PREF_VAL_OPEN_WITH_CUSTOM_TAB

    val isOpenItemWithReader: Boolean
        get() = articleOpener==PREF_VAL_OPEN_WITH_READER

    val isOpenItemWithAppDefault: Boolean
        get() = when (articleOpener) {
            PREF_VAL_OPEN_WITH_READER,
            PREF_VAL_OPEN_WITH_WEBVIEW,
            PREF_VAL_OPEN_WITH_BROWSER,
            PREF_VAL_OPEN_WITH_CUSTOM_TAB,
            -> false
            else -> true
        }

    val isNotValidUrl = !isValidUrl

    private val isValidUrl: Boolean
        get() {
            return try {
                URL(url)
                true
            } catch (e: Exception) {
                false
            }
        }

    val isOkToSave: Boolean
        get() = isValidUrl
}
