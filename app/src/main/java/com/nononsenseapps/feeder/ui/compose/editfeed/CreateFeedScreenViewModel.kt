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
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.db.room.SelectableFolder
import com.nononsenseapps.feeder.model.ArticleRuleError
import com.nononsenseapps.feeder.model.ArticleRuleSet
import com.nononsenseapps.feeder.ui.compose.utils.mutableSavedStateOf
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURLOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import java.net.URL
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CreateFeedScreenViewModel(
    di: DI,
    state: SavedStateHandle,
) : DIAwareViewModel(di),
    EditFeedScreenState {
    private val repository: Repository by instance()

    // These three are updated as a result of url/uri updating
    override var isNotValidUrl by mutableStateOf(false)
    override var isOkToSave: Boolean by mutableStateOf(false)

    override var feedUrl: String by mutableSavedStateOf(state, "") { value ->
        isNotValidUrl = !isValidUrlOrUri(value)
        isOkToSave = isValidUrlOrUri(value)
    }
    override var feedTitle: String by mutableSavedStateOf(state, "")
    override var folderId: Long by mutableSavedStateOf(state, ID_UNSET)
    override var folderName: String by mutableSavedStateOf(state, "")
    override var fullTextByDefault: Boolean by mutableSavedStateOf(state, false)
    override var skipDuplicates: Boolean by mutableSavedStateOf(state, false)
    override var notify: Boolean by mutableSavedStateOf(state, false)
    override var articleOpener: String by mutableSavedStateOf(state, "")
    override var alternateId: Boolean by mutableSavedStateOf(state, false)
    override var summarizeOnOpen: Boolean by mutableSavedStateOf(state, false)
    override var fetchOgImages: Boolean by mutableSavedStateOf(state, false)
    override var trustAllCertificates: Boolean by mutableSavedStateOf(state, false)

    // Invalid rules deliberately do not block saving - a regex is invalid while being
    // typed, and the engine ignores invalid lines anyway.
    override var blockRulesError: ArticleRuleError? by mutableStateOf(null)
    override var allowRulesError: ArticleRuleError? by mutableStateOf(null)
    override var blockRules: String by mutableSavedStateOf(state, "") { value ->
        blockRulesError = ArticleRuleSet.parse(value).errors.firstOrNull()
    }
    override var allowRules: String by mutableSavedStateOf(state, "") { value ->
        allowRulesError = ArticleRuleSet.parse(value).errors.firstOrNull()
    }
    override var showFolderDialog: Boolean by mutableSavedStateOf(state, false)
    override var allFolders: List<SelectableFolder> by mutableStateOf(emptyList())
    override var defaultTitle: String by mutableStateOf(state["feedTitle"] ?: "")
    override var feedImage: String by mutableStateOf(state["feedImage"] ?: "")

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
        repository.allFolders
            .onEach { value ->
                allFolders = value
            }.launchIn(viewModelScope)

        state
            .getMutableStateFlow("folderId", ID_UNSET)
            .flatMapLatest { folderId ->
                if (folderId > ID_UNSET) {
                    repository.getFolderNameFlow(folderId)
                } else {
                    flowOf("")
                }
            }.onEach { name ->
                if (name != null) {
                    folderName = name
                } else {
                    // Reset because folder does not exist
                    folderId = ID_UNSET
                }
            }.launchIn(viewModelScope)
    }

    fun saveAndRequestSync(action: (Long) -> Unit) =
        viewModelScope.launch {
            val feedId =
                repository.saveFeed(
                    Feed(
                        url = URL(feedUrl),
                        title = feedTitle,
                        customTitle = feedTitle,
                        folderIdNullable = folderId.takeUnless { it == ID_UNSET },
                        fullTextByDefault = fullTextByDefault,
                        notify = notify,
                        skipDuplicates = skipDuplicates,
                        openArticlesWith = articleOpener,
                        alternateId = alternateId,
                        whenModified = Instant.now(),
                        imageUrl = sloppyLinkToStrictURLOrNull(feedImage),
                        summarizeOnOpen = summarizeOnOpen,
                        fetchOgImages = fetchOgImages,
                        trustAllCertificates = trustAllCertificates,
                        blockRules = blockRules,
                        allowRules = allowRules,
                    ),
                )

            runOnceRssSync(
                di = di,
                feedId = feedId,
                triggeredByUser = false,
            )

            action(feedId)
        }
}
