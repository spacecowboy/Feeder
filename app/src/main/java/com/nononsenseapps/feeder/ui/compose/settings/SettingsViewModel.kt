package com.nononsenseapps.feeder.ui.compose.settings

import android.app.Application
import android.os.PowerManager
import androidx.compose.runtime.Immutable
import androidx.core.content.getSystemService
import androidx.lifecycle.viewModelScope
import com.nononsenseapps.feeder.ApplicationCoroutineScope
import com.nononsenseapps.feeder.archmodel.DarkThemePreferences
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.archmodel.ItemOpener
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.archmodel.SortingOptions
import com.nononsenseapps.feeder.archmodel.SwipeAsRead
import com.nononsenseapps.feeder.archmodel.SyncFrequency
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.openai.OpenAIApi
import com.nononsenseapps.feeder.openai.canUseAsTranslationApi
import com.nononsenseapps.feeder.ui.compose.settings.FontSelection
import com.nononsenseapps.feeder.ui.compose.settings.FontSelection.SystemDefault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class SettingsViewModel(
    di: DI,
) : DIAwareViewModel(di) {
    private val repository: Repository by instance()
    private val context: Application by instance()
    private val applicationCoroutineScope: ApplicationCoroutineScope by instance()
    private val openAIApi: OpenAIApi by instance()

    fun setCurrentTheme(value: ThemeOptions) {
        repository.setCurrentTheme(value)
    }

    fun setPreferredDarkTheme(value: DarkThemePreferences) {
        repository.setPreferredDarkTheme(value)
    }

    fun setCurrentSorting(value: SortingOptions) {
        repository.setCurrentSorting(value)
    }

    fun setShowFab(value: Boolean) {
        repository.setShowFab(value)
    }

    fun setSyncOnResume(value: Boolean) {
        repository.setSyncOnResume(value)
    }

    fun setSyncOnlyOnWifi(value: Boolean) =
        applicationCoroutineScope.launch {
            repository.setSyncOnlyOnWifi(value)
        }

    fun setSyncOnlyWhenCharging(value: Boolean) =
        applicationCoroutineScope.launch {
            repository.setSyncOnlyWhenCharging(value)
        }

    fun setLoadImageOnlyOnWifi(value: Boolean) {
        repository.setLoadImageOnlyOnWifi(value)
    }

    fun setShowThumbnails(value: Boolean) {
        repository.setShowThumbnails(value)
    }

    fun setUseDetectLanguage(value: Boolean) {
        repository.setUseDetectLanguage(value)
    }

    fun setUseDynamicTheme(value: Boolean) {
        repository.setUseDynamicTheme(value)
    }

    fun setMaxCountPerFeed(value: Int) {
        repository.setMaxCountPerFeed(value)
    }

    fun setItemOpener(value: ItemOpener) {
        repository.setItemOpener(value)
    }

    fun setLinkOpener(value: LinkOpener) {
        repository.setLinkOpener(value)
    }

    fun setSyncFrequency(value: SyncFrequency) =
        applicationCoroutineScope.launch {
            repository.setSyncFrequency(value)
        }

    fun setFeedItemStyle(value: FeedItemStyle) {
        repository.setFeedItemStyle(value)
    }

    fun addToBlockList(value: String) =
        applicationCoroutineScope.launch {
            repository.addBlocklistPattern(value)
        }

    fun removeFromBlockList(value: String) =
        applicationCoroutineScope.launch {
            repository.removeBlocklistPattern(value)
        }

    fun setApplyBlocklistToSummaries(value: Boolean) =
        applicationCoroutineScope.launch {
            repository.setApplyBlocklistToSummaries(value)
        }

    fun toggleNotifications(
        feedId: Long,
        value: Boolean,
    ) = applicationCoroutineScope.launch {
        repository.toggleNotifications(feedId, value)
    }

    fun setSwipeAsRead(value: SwipeAsRead) {
        repository.setSwipeAsRead(value)
    }

    fun setIsMarkAsReadOnScroll(value: Boolean) {
        repository.setIsMarkAsReadOnScroll(value)
    }

    fun setMaxLines(value: Int) {
        repository.setMaxLines(value)
    }

    fun setShowOnlyTitles(value: Boolean) {
        repository.setShowOnlyTitles(value)
    }

    fun setIsOpenAdjacent(value: Boolean) {
        repository.setOpenAdjacent(value)
    }

    fun setShowReadingTime(value: Boolean) {
        repository.setShowReadingTime(value)
    }

    fun setShowTitleUnreadCount(value: Boolean) {
        repository.setShowTitleUnreadCount(value)
    }

    fun setOpenDrawerOnFab(value: Boolean) {
        repository.setOpenDrawerOnFab(value)
    }

    fun setTranslateFeedCardsByDefault(value: Boolean) {
        repository.setTranslateFeedCardsByDefault(value)
    }

    fun setTranslateArticlesByDefault(value: Boolean) {
        repository.setTranslateArticlesByDefault(value)
    }

    fun setIsPagingMode(value: Boolean) {
        repository.setIsPagingMode(value)
    }

    fun setIsAnimatedPaging(value: Boolean) {
        repository.setIsAnimatedPaging(value)
    }

    fun onSummaryOpenAISettingsEvent(event: OpenAISettingsEvent) {
        handleOpenAISettingsEvent(
            event = event,
            modelsState = summaryOpenAIModelsState,
            currentState = { _viewState.value.summaryAIState },
            updateState = { updatedState -> _viewState.value = _viewState.value.copy(summaryAIState = updatedState) },
            saveSettings = repository::setOpenAiSettings,
        )
    }

    fun onTranslationApiSettingsEvent(event: TranslationApiSettingsEvent) {
        handleOpenAISettingsEvent(
            event = event,
            modelsState = translationApiModelsState,
            currentState = { _viewState.value.translationApiState },
            updateState = { updatedState -> _viewState.value = _viewState.value.copy(translationApiState = updatedState) },
            saveSettings = repository::setTranslationApiSettings,
        )
    }

    fun setPreferredTranslationLanguage(value: String) {
        repository.setPreferredTranslationLanguage(value)
    }

    private val summaryOpenAIModelsState = MutableStateFlow<OpenAIModelsState>(OpenAIModelsState.None)
    private val translationApiModelsState = MutableStateFlow<TranslationApiModelsState>(OpenAIModelsState.None)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val immutableFeedsSettings =
        repository.feedNotificationSettings
            .mapLatest { values ->
                values.map {
                    UIFeedSettings(
                        feedId = it.id,
                        title = it.title,
                        notify = it.notify,
                    )
                }
            }

    private val batteryOptimizationIgnoredFlow: Flow<Boolean> =
        repository.resumeTime
            .map {
                val powerManager: PowerManager? = context.getSystemService()
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
            }.buffer(1)

    private val _viewState = MutableStateFlow(SettingsViewState())
    val viewState: StateFlow<SettingsViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.currentTheme,
                repository.preferredDarkTheme,
                repository.currentSorting,
                repository.showFab,
                repository.syncOnResume,
                repository.syncOnlyOnWifi,
                repository.syncOnlyWhenCharging,
                repository.loadImageOnlyOnWifi,
                repository.showThumbnails,
                repository.maximumCountPerFeed,
                repository.itemOpener,
                repository.linkOpener,
                repository.syncFrequency,
                batteryOptimizationIgnoredFlow,
                repository.feedItemStyle,
                repository.swipeAsRead,
                repository.blockList,
                repository.applyBlocklistToSummaries,
                repository.useDetectLanguage,
                repository.useDynamicTheme,
                immutableFeedsSettings,
                repository.isMarkAsReadOnScroll,
                repository.maxLines,
                repository.showOnlyTitle,
                repository.isOpenAdjacent,
                repository.showReadingTime,
                repository.showTitleUnreadCount,
                repository.openAISettings,
                summaryOpenAIModelsState,
                repository.translationApiSettings,
                repository.preferredTranslationLanguage,
                translationApiModelsState,
                repository.isOpenDrawerOnFab,
                repository.translateFeedCardsByDefault,
                repository.translateArticlesByDefault,
                repository.font,
                repository.isPagingMode,
                repository.isAnimatedPaging,
            ) { params: Array<Any> ->
                @Suppress("UNCHECKED_CAST")
                SettingsViewState(
                    currentTheme = params[0] as ThemeOptions,
                    darkThemePreference = params[1] as DarkThemePreferences,
                    currentSorting = params[2] as SortingOptions,
                    showFab = params[3] as Boolean,
                    syncOnResume = params[4] as Boolean,
                    syncOnlyOnWifi = params[5] as Boolean,
                    syncOnlyWhenCharging = params[6] as Boolean,
                    loadImageOnlyOnWifi = params[7] as Boolean,
                    showThumbnails = params[8] as Boolean,
                    maximumCountPerFeed = params[9] as Int,
                    itemOpener = params[10] as ItemOpener,
                    linkOpener = params[11] as LinkOpener,
                    syncFrequency = params[12] as SyncFrequency,
                    batteryOptimizationIgnored = params[13] as Boolean,
                    feedItemStyle = params[14] as FeedItemStyle,
                    swipeAsRead = params[15] as SwipeAsRead,
                    blockList = params[16] as List<String>,
                    applyBlocklistToSummaries = params[17] as Boolean,
                    useDetectLanguage = params[18] as Boolean,
                    useDynamicTheme = params[19] as Boolean,
                    feedsSettings = params[20] as List<UIFeedSettings>,
                    isMarkAsReadOnScroll = params[21] as Boolean,
                    maxLines = params[22] as Int,
                    showOnlyTitle = params[23] as Boolean,
                    isOpenAdjacent = params[24] as Boolean,
                    showReadingTime = params[25] as Boolean,
                    showTitleUnreadCount = params[26] as Boolean,
                    summaryAIState =
                        _viewState.value.summaryAIState.copy(
                            settings = params[27] as OpenAISettings,
                            modelsResult = params[28] as OpenAIModelsState,
                        ),
                    translationApiState =
                        _viewState.value.translationApiState.copy(
                            settings = params[29] as OpenAISettings,
                            modelsResult = params[31] as OpenAIModelsState,
                        ),
                    preferredTranslationLanguage = params[30] as String,
                    canTranslate = (params[29] as OpenAISettings).canUseAsTranslationApi && (params[30] as String).trim().isNotBlank(),
                    isOpenDrawerOnFab = params[32] as Boolean,
                    translateFeedCardsByDefault = params[33] as Boolean,
                    translateArticlesByDefault = params[34] as Boolean,
                    font = params[35] as FontSelection,
                    isPagingMode = params[36] as Boolean,
                    isAnimatedPaging = params[37] as Boolean,
                )
            }.collect {
                _viewState.value = it
            }
        }
    }

    private fun loadOpenAIModels(
        stateFlow: MutableStateFlow<OpenAIModelsState>,
        settings: OpenAISettings,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            stateFlow.value = OpenAIModelsState.Loading(settings)
            stateFlow.value =
                openAIApi.listModelIds(settings).let { res ->
                    when (res) {
                        is OpenAIApi.ModelsResult.Error -> OpenAIModelsState.Error(res.message ?: "", settings)
                        OpenAIApi.ModelsResult.MissingToken -> OpenAIModelsState.None
                        is OpenAIApi.ModelsResult.Success -> OpenAIModelsState.Success(res.ids, settings)
                        OpenAIApi.ModelsResult.AzureApiVersionRequired -> OpenAIModelsState.None
                        OpenAIApi.ModelsResult.AzureDeploymentIdRequired -> OpenAIModelsState.None
                    }
                }
        }
    }

    private fun handleOpenAISettingsEvent(
        event: OpenAISettingsEvent,
        modelsState: MutableStateFlow<OpenAIModelsState>,
        currentState: () -> OpenAISettingsState,
        updateState: (OpenAISettingsState) -> Unit,
        saveSettings: (OpenAISettings) -> Unit,
    ) {
        when (event) {
            is OpenAISettingsEvent.LoadModels -> loadOpenAIModels(modelsState, event.settings)
            is OpenAISettingsEvent.UpdateSettings -> saveSettings(event.settings)
            is OpenAISettingsEvent.SwitchEditMode -> {
                val current = currentState()
                updateState(current.copy(isEditMode = event.enabled))
            }
            is OpenAISettingsEvent.ShowModelsError -> {
                val current = currentState()
                updateState(current.copy(showModelsError = event.show))
            }
        }
    }

    companion object {
        @Suppress("unused")
        private const val LOG_TAG = "FEEDER_SETTINGSVM"
    }
}

@Immutable
data class SettingsViewState(
    val currentTheme: ThemeOptions = ThemeOptions.SYSTEM,
    val darkThemePreference: DarkThemePreferences = DarkThemePreferences.BLACK,
    val currentSorting: SortingOptions = SortingOptions.NEWEST_FIRST,
    val showFab: Boolean = true,
    val feedItemStyle: FeedItemStyle = FeedItemStyle.CARD,
    val blockList: List<String> = emptyList(),
    val applyBlocklistToSummaries: Boolean = false,
    val syncOnResume: Boolean = false,
    val syncOnlyOnWifi: Boolean = false,
    val syncOnlyWhenCharging: Boolean = false,
    val loadImageOnlyOnWifi: Boolean = false,
    val showThumbnails: Boolean = false,
    val maximumCountPerFeed: Int = 100,
    val itemOpener: ItemOpener = ItemOpener.READER,
    val linkOpener: LinkOpener = LinkOpener.CUSTOM_TAB,
    val syncFrequency: SyncFrequency = SyncFrequency.EVERY_1_HOURS,
    val batteryOptimizationIgnored: Boolean = false,
    val swipeAsRead: SwipeAsRead = SwipeAsRead.ONLY_FROM_END,
    val useDetectLanguage: Boolean = true,
    val useDynamicTheme: Boolean = true,
    val feedsSettings: List<UIFeedSettings> = emptyList(),
    val isMarkAsReadOnScroll: Boolean = false,
    val maxLines: Int = 2,
    val showOnlyTitle: Boolean = false,
    val isOpenAdjacent: Boolean = true,
    val summaryAIState: OpenAISettingsState = OpenAISettingsState(),
    val translationApiState: TranslationApiSettingsState = TranslationApiSettingsState(),
    val preferredTranslationLanguage: String = "",
    val canTranslate: Boolean = false,
    val showReadingTime: Boolean = false,
    val showTitleUnreadCount: Boolean = false,
    val isOpenDrawerOnFab: Boolean = false,
    val translateFeedCardsByDefault: Boolean = false,
    val translateArticlesByDefault: Boolean = false,
    val font: FontSelection = SystemDefault,
    val isPagingMode: Boolean = false,
    val isAnimatedPaging: Boolean = false,
)

data class UIFeedSettings(
    val feedId: Long,
    val title: String,
    val notify: Boolean,
)

data class OpenAISettingsState(
    val settings: OpenAISettings = OpenAISettings(),
    val modelsResult: OpenAIModelsState = OpenAIModelsState.None,
    val isEditMode: Boolean = false,
    val showModelsError: Boolean = false,
)

sealed interface OpenAIModelsState {
    data object None : OpenAIModelsState

    data class Loading(
        val settings: OpenAISettings,
    ) : OpenAIModelsState

    data class Success(
        val ids: List<String>,
        val settings: OpenAISettings,
    ) : OpenAIModelsState

    data class Error(
        val message: String,
        val settings: OpenAISettings,
    ) : OpenAIModelsState
}

sealed interface OpenAISettingsEvent {
    data class UpdateSettings(
        val settings: OpenAISettings,
    ) : OpenAISettingsEvent

    data class LoadModels(
        val settings: OpenAISettings,
    ) : OpenAISettingsEvent

    data class SwitchEditMode(
        val enabled: Boolean,
    ) : OpenAISettingsEvent

    data class ShowModelsError(
        val show: Boolean,
    ) : OpenAISettingsEvent
}

typealias TranslationApiSettingsState = OpenAISettingsState

typealias TranslationApiModelsState = OpenAIModelsState

typealias TranslationApiSettingsEvent = OpenAISettingsEvent
