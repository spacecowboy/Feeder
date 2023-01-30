package com.nononsenseapps.feeder.ui.compose.editfeed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.model.workmanager.requestFeedSync
import java.net.URL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.instance
import org.threeten.bp.Instant

class CreateFeedScreenViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val repository: Repository by instance()

    private val _url: MutableStateFlow<String> = MutableStateFlow(
        state["feedUrl"] ?: ""
    )
    fun setUrl(value: String) {
        state["feedUrl"] = value
        _url.update { value }
    }

    private val _tag: MutableStateFlow<String> = MutableStateFlow(
        state["feedTag"] ?: ""
    )
    fun setTag(value: String) {
        state["feedTag"] = value
        _tag.update { value }
    }

    private val _title: MutableStateFlow<String> = MutableStateFlow(
        state["feedTitle"] ?: ""
    )
    fun setTitle(value: String) {
        state["feedTitle"] = value
        _title.update { value }
    }

    private val _fullTextByDefault: MutableStateFlow<Boolean> = MutableStateFlow(
        state["fullTextByDefault"] ?: false
    )
    fun setFullTextByDefault(value: Boolean) {
        state["fullTextByDefault"] = value
        _fullTextByDefault.update { value }
    }

    private val _notify: MutableStateFlow<Boolean> = MutableStateFlow(
        state["notify"] ?: false
    )
    fun setNotify(value: Boolean) {
        state["notify"] = value
        _notify.update { value }
    }

    private val _articleOpener: MutableStateFlow<String> = MutableStateFlow(
        state["articleOpener"] ?: ""
    )
    fun setArticleOpener(value: String) {
        state["articleOpener"] = value
        _articleOpener.update { value }
    }

    private val _alternateId: MutableStateFlow<Boolean> = MutableStateFlow(
        state["alternateId"] ?: false
    )
    fun setAlternateId(value: Boolean) {
        state["alternateId"] = value
        _alternateId.update { value }
    }

    fun saveAndRequestSync(): Long {
        val url = _url.value

        val feedId = runBlocking {
            repository.saveFeed(
                Feed(
                    url = URL(url),
                    title = _title.value,
                    customTitle = _title.value,
                    tag = _tag.value,
                    fullTextByDefault = _fullTextByDefault.value,
                    notify = _notify.value,
                    openArticlesWith = _articleOpener.value,
                    alternateId = _alternateId.value,
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
                _tag,
                _url,
                _title,
                _fullTextByDefault,
                _notify,
                _articleOpener,
                _alternateId,
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
                    alternateId = params[7] as Boolean,
                    defaultTitle = "",
                )
            }.collect {
                _viewState.value = it
            }
        }
    }
}
