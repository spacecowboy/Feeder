package com.nononsenseapps.feeder.ui.compose.editfeed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_READER
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.model.requestFeedSync
import java.net.URL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.instance
import org.threeten.bp.Instant

class CreateFeedScreenViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val repository: Repository by instance()

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

    private val _articleOpener = state.getLiveData("articleOpener", "")
    fun setArticleOpener(value: String) {
        state["articleOpener"] = value
    }

    private val _alternateId = state.getLiveData("alternateId", false)
    fun setAlternateId(value: Boolean) {
        state["alternateId"] = value
    }

    fun setCurrentFeedAndTag(feedId: Long, tag: String) {
        repository.setCurrentFeedAndTag(feedId, tag)
    }

    fun saveAndRequestSync(): Long {
        val url = _url.value
            ?: error("Missing url in state!!!")

        val feedId = runBlocking {
            repository.saveFeed(
                Feed(
                    url = URL(url),
                    title = _title.value ?: "",
                    customTitle = _title.value ?: "",
                    tag = _tag.value ?: "",
                    fullTextByDefault = _fullTextByDefault.value ?: false,
                    notify = _notify.value ?: false,
                    openArticlesWith = _articleOpener.value ?: PREF_VAL_OPEN_WITH_READER,
                    alternateId = _alternateId.value ?: false,
                    whenModified = Instant.now(),
                )
            )
        }
        requestFeedSync(
            di,
            feedId = feedId
        )
        return feedId
    }

    private val _viewState = MutableStateFlow(EditFeedViewState())
    val viewState: StateFlow<EditFeedViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.allTags,
                _tag.asFlow(),
                _url.asFlow(),
                _title.asFlow(),
                _fullTextByDefault.asFlow(),
                _notify.asFlow(),
                _articleOpener.asFlow(),
                _alternateId.asFlow(),
            )
            { params: Array<Any> ->
                @Suppress("UNCHECKED_CAST")
                EditFeedViewState(
                    allTags = params[0] as List<String>,
                    tag = params[1] as String,
                    url = params[2] as String,
                    title = params[3] as String,
                    fullTextByDefault = params[4] as Boolean,
                    notify = params[5] as Boolean,
                    articleOpener = params[6] as String,
                    alternateId = params[7] as Boolean,
                    defaultTitle = "",
                )
            }.collect {
                _viewState.value = it
            }
        }
    }
}
