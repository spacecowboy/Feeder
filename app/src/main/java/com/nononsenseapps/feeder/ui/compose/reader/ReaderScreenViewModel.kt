package com.nononsenseapps.feeder.ui.compose.reader

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.blob.blobFullFile
import com.nononsenseapps.feeder.db.room.FeedItemForFetching
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.model.parseFullArticleIfMissing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ReaderScreenViewModel(di: DI, private val state: SavedStateHandle) : DIAwareViewModel(di) {
    private val repository: Repository by instance()

    val currentItemId: Long = state["itemId"]
        ?: throw IllegalArgumentException("Missing item id in state!")

    suspend fun markAsReadAndNotified() {
        repository.markAsReadAndNotified(currentItemId)
    }

    fun markCurrentItemAsUnreadInBackground() {
        viewModelScope.launch {
            repository.markAsUnread(currentItemId)
        }
    }

    private val _textToDisplay = state.getLiveData("textToDisplay", TextToDisplay.DEFAULT)
    fun displayArticleText() {
        state["textToDisplay"] = TextToDisplay.DEFAULT
    }

    fun displayFullText() {
        viewModelScope.launch {
            loadFullTextThenDisplayIt()
        }
    }

    suspend fun loadFullTextThenDisplayIt() {
        val filesDir = getApplication<FeederApplication>().filesDir

        if (blobFullFile(currentItemId, filesDir).isFile) {
            state["textToDisplay"] = TextToDisplay.FULLTEXT
            return
        }

        state["textToDisplay"] = TextToDisplay.LOADING_FULLTEXT
        val link = repository.getLink(currentItemId)
        val result = parseFullArticleIfMissing(
            object : FeedItemForFetching {
                override val id = currentItemId
                override val link = link
            },
            di.direct.instance(),
            filesDir,
        )

        state["textToDisplay"] = when (result) {
            true -> TextToDisplay.FULLTEXT
            false -> TextToDisplay.FAILED_TO_LOAD_FULLTEXT
        }
    }

    private val _viewState = MutableStateFlow(ReaderScreenViewState())
    val viewState: StateFlow<ReaderScreenViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            if (!state.contains("textToDisplay")) {
                // Set initial state according to item
                val itemPreferredText = repository.getTextToDisplayForItem(currentItemId)
                if (itemPreferredText == TextToDisplay.FULLTEXT) {
                    loadFullTextThenDisplayIt()
                }
            }
            combine(
                _textToDisplay.asFlow(),
                repository.getFeedItem(currentItemId),
                repository.linkOpener,
            ) { textToDisplay, feedItem, linkOpener ->
                // Should not be null but don't crash if it is
                ReaderScreenViewState(
                    textToDisplay = textToDisplay,
                    currentItem = feedItem ?: FeedItemWithFeed(),
                    linkOpener = linkOpener
                )
            }.collect {
                _viewState.value = it
            }
        }
    }
}

@Immutable
data class ReaderScreenViewState(
    val currentItem: FeedItemWithFeed = FeedItemWithFeed(),
    val linkOpener: LinkOpener = LinkOpener.CUSTOM_TAB,
    // Set by default by item itself - but has a state set in SavedState which overrides it
    val textToDisplay: TextToDisplay = TextToDisplay.DEFAULT,
)

enum class TextToDisplay {
    DEFAULT,
    LOADING_FULLTEXT,
    FAILED_TO_LOAD_FULLTEXT,
    FULLTEXT,
}
