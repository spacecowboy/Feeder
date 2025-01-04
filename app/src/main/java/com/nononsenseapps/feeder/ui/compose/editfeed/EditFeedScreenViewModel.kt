package com.nononsenseapps.feeder.ui.compose.editfeed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_BROWSER
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_CUSTOM_TAB
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_READER
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_WEBVIEW
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.background.runOnceRssSync
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.ui.compose.utils.mutableSavedStateOf
import com.nononsenseapps.feeder.util.getOrCreateFromUri
import com.nononsenseapps.feeder.util.isNostrUri
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import java.net.URL
import java.time.Instant

class EditFeedScreenViewModel(
    di: DI,
    private val state: SavedStateHandle,
) : DIAwareViewModel(di), EditFeedScreenState {
    private val repository: Repository by instance()

    val feedId: Long =
        state["feedId"]
            ?: throw IllegalArgumentException("Missing feedId in savedState")

    // These two are updated as a result of url updating
    override var isNotValidUrl by mutableStateOf(false)
    override var isOkToSave: Boolean by mutableStateOf(false)

    override var feedUrl: String by mutableSavedStateOf(state, "") { value ->
        isNotValidUrl = !isValidUrlOrUri(value)
        isOkToSave = isValidUrlOrUri(value)
    }
    override var feedTitle: String by mutableSavedStateOf(state, "")
    override var feedTag: String by mutableSavedStateOf(state, "")
    override var fullTextByDefault: Boolean by mutableSavedStateOf(state, false)
    override var skipDuplicates: Boolean by mutableSavedStateOf(state, false)
    override var notify: Boolean by mutableSavedStateOf(state, false)
    override var articleOpener: String by mutableSavedStateOf(state, "")
    override var alternateId: Boolean by mutableSavedStateOf(state, false)
    override var allTags: List<String> by mutableStateOf(emptyList())

    override var feedImage: String by mutableStateOf("")

    // Only set when loading feed
    override var defaultTitle: String by mutableStateOf("")

    override val isOpenItemWithBrowser: Boolean
        get() = articleOpener == PREF_VAL_OPEN_WITH_BROWSER

    override val isOpenItemWithCustomTab: Boolean
        get() = articleOpener == PREF_VAL_OPEN_WITH_CUSTOM_TAB

    override val isOpenItemWithReader: Boolean
        get() = articleOpener == PREF_VAL_OPEN_WITH_READER

    override val isOpenItemWithAppDefault: Boolean
        get() =
            when (articleOpener) {
                PREF_VAL_OPEN_WITH_READER,
                PREF_VAL_OPEN_WITH_WEBVIEW,
                PREF_VAL_OPEN_WITH_BROWSER,
                PREF_VAL_OPEN_WITH_CUSTOM_TAB,
                -> false

                else -> true
            }

    init {
        viewModelScope.launch {
            // Set initial state in case state is empty
            val feed =
                repository.getFeed(feedId)
                    ?: throw IllegalArgumentException("No feed with id $feedId!")

            defaultTitle = feed.title
            feedImage = feed.imageUrl?.toString() ?: ""

            if (!state.contains("feedUrl")) {
                feedUrl = feed.url.toString()
            }
            if (!state.contains("feedTag")) {
                feedTag = feed.tag
            }
            if (!state.contains("feedTitle")) {
                feedTitle = feed.displayTitle
            }
            if (!state.contains("fullTextByDefault")) {
                fullTextByDefault = feed.fullTextByDefault
            }
            if (!state.contains("notify")) {
                notify = feed.notify
            }
            if (!state.contains("skipDuplicates")) {
                skipDuplicates = feed.skipDuplicates
            }
            if (!state.contains("articleOpener")) {
                articleOpener = feed.openArticlesWith
            }
            if (!state.contains("alternateId")) {
                alternateId = feed.alternateId
            }

            repository.allTags
                .collect { value ->
                    allTags = value
                }
        }
    }

    fun saveAndRequestSync(action: (Long) -> Unit) =
        viewModelScope.launch {
            val feed =
                repository.getFeed(feedId)
                    ?: Feed() // Feed was deleted while being edited?

            val updatedFeed =
                feed.copy(
                    url = URL(feedUrl.getOrCreateFromUri()),
                    title = feedTitle,
                    customTitle = feedTitle,
                    tag = feedTag,
                    fullTextByDefault = if (feedUrl.isNostrUri()) false else fullTextByDefault,
                    notify = notify,
                    skipDuplicates = skipDuplicates,
                    openArticlesWith = articleOpener,
                    alternateId = alternateId,
                )

            // No point in doing anything unless they actually differ
            if (feed != updatedFeed) {
                // In case clocks between different devices differ don't allow this date to go backwards
                updatedFeed.whenModified = maxOf(Instant.now(), feed.whenModified.plusMillis(1))
                val savedId =
                    repository.saveFeed(
                        updatedFeed,
                    )
                runOnceRssSync(
                    di = di,
                    feedId = savedId,
                    triggeredByUser = false,
                )
            }

            action(feed.id)
        }
}

internal fun isValidUrlOrUri(value: String): Boolean {
    return try {
        if (value.isNostrUri()) {
            true
        } else {
            URL(value)
            true
        }
    } catch (e: Exception) {
        false
    }
}
