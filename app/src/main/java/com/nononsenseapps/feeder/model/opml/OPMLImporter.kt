package com.nononsenseapps.feeder.model.opml

import android.util.Log
import com.nononsenseapps.feeder.archmodel.SettingsStore
import com.nononsenseapps.feeder.archmodel.UserSettings
import com.nononsenseapps.feeder.archmodel.darkThemePreferenceFromString
import com.nononsenseapps.feeder.archmodel.defaultFont
import com.nononsenseapps.feeder.archmodel.feedItemStyleFromString
import com.nononsenseapps.feeder.archmodel.itemOpenerFromString
import com.nononsenseapps.feeder.archmodel.linkOpenerFromString
import com.nononsenseapps.feeder.archmodel.sortingOptionsFromString
import com.nononsenseapps.feeder.archmodel.swipeAsReadFromString
import com.nononsenseapps.feeder.archmodel.syncFrequencyFromString
import com.nononsenseapps.feeder.archmodel.themeOptionsFromString
import com.nononsenseapps.feeder.db.room.Feed
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.model.OPMLParserHandler
import com.nononsenseapps.feeder.util.FilePathProvider
import kotlinx.coroutines.flow.first
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

open class OPMLImporter(
    override val di: DI,
) : OPMLParserHandler,
    DIAware {
    private val feedDao: FeedDao by instance()
    private val settingsStore: SettingsStore by instance()
    private val filePathProvider: FilePathProvider by instance()

    override suspend fun saveFeed(feed: Feed) {
        val existing = feedDao.loadFeedWithUrl(feed.url)
        // Don't want to remove existing feed on OPML imports
        if (existing != null) {
            feedDao.updateFeed(feed.copy(id = existing.id))
        } else {
            feedDao.insertFeed(feed)
        }
    }

    override suspend fun saveSetting(
        key: String,
        value: String,
    ) {
        when (UserSettings.fromKey(key)) {
            null -> Log.w(LOG_TAG, "Unrecognized setting during import: $key")
            UserSettings.SETTING_ADDED_FEEDER_NEWS -> settingsStore.setAddedFeederNews(value.toBoolean())
            UserSettings.SETTING_THEME ->
                settingsStore.setCurrentTheme(
                    themeOptionsFromString(value),
                )
            UserSettings.SETTING_DARK_THEME ->
                settingsStore.setDarkThemePreference(
                    darkThemePreferenceFromString(value),
                )
            UserSettings.SETTING_DYNAMIC_THEME -> settingsStore.setUseDynamicTheme(value.toBoolean())
            UserSettings.SETTING_SORT ->
                settingsStore.setCurrentSorting(
                    sortingOptionsFromString(value),
                )
            UserSettings.SETTING_SHOW_FAB -> settingsStore.setShowFab(value.toBoolean())
            UserSettings.SETTING_FEED_ITEM_STYLE ->
                settingsStore.setFeedItemStyle(
                    feedItemStyleFromString(value),
                )
            UserSettings.SETTING_SWIPE_AS_READ ->
                settingsStore.setSwipeAsRead(
                    swipeAsReadFromString(value),
                )
            UserSettings.SETTING_SYNC_ONLY_CHARGING -> settingsStore.setSyncOnlyWhenCharging(value.toBoolean())
            UserSettings.SETTING_SYNC_ONLY_WIFI -> settingsStore.setSyncOnlyOnWifi(value.toBoolean())
            UserSettings.SETTING_SYNC_FREQ ->
                settingsStore.setSyncFrequency(
                    syncFrequencyFromString(value),
                )
            UserSettings.SETTING_SYNC_ON_RESUME -> settingsStore.setSyncOnResume(value.toBoolean())
            UserSettings.SETTING_IMG_ONLY_WIFI -> settingsStore.setLoadImageOnlyOnWifi(value.toBoolean())
            UserSettings.SETTING_IMG_SHOW_THUMBNAILS -> settingsStore.setShowThumbnails(value.toBoolean())
            UserSettings.SETTING_DEFAULT_OPEN_ITEM_WITH ->
                settingsStore.setItemOpener(
                    itemOpenerFromString(value),
                )
            UserSettings.SETTING_OPEN_LINKS_WITH ->
                settingsStore.setLinkOpener(
                    linkOpenerFromString(value),
                )
            UserSettings.SETTING_TEXT_SCALE -> settingsStore.setTextScale(value.toFloatOrNull() ?: 1.0f)
            UserSettings.SETTING_IS_MARK_AS_READ_ON_SCROLL -> settingsStore.setIsMarkAsReadOnScroll(value.toBoolean())
            UserSettings.SETTING_READALOUD_USE_DETECT_LANGUAGE -> settingsStore.setUseDetectLanguage(value.toBoolean())
            UserSettings.SETTING_MAX_LINES -> settingsStore.setMaxLines((value.toIntOrNull() ?: 1).coerceAtLeast(1))
            UserSettings.SETTINGS_FILTER_SAVED -> settingsStore.setFeedListFilterSaved(value.toBoolean())
            UserSettings.SETTINGS_FILTER_RECENTLY_READ -> settingsStore.setFeedListFilterRecentlyRead(value.toBoolean())
            UserSettings.SETTINGS_FILTER_READ -> settingsStore.setFeedListFilterRead(value.toBoolean())
            UserSettings.SETTINGS_LIST_SHOW_ONLY_TITLES -> settingsStore.setShowOnlyTitles(value.toBoolean())
            UserSettings.SETTING_OPEN_ADJACENT -> settingsStore.setOpenAdjacent(value.toBoolean())
            UserSettings.SETTING_FONT ->
                settingsStore.setFont(
                    com.nononsenseapps.feeder.ui.compose.settings.getFontSelectionFromPath(
                        filePathProvider,
                        value,
                    ) ?: defaultFont,
                )
            UserSettings.SETTING_LIST_SHOW_READING_TIME -> settingsStore.setShowReadingTime(value.toBoolean())
            UserSettings.SETTING_OPEN_DRAWER_ON_FAB -> settingsStore.setOpenDrawerOnFab(value.toBoolean())
            UserSettings.SETTING_SHOW_TITLE_UNREAD_COUNT -> settingsStore.setShowTitleUnreadCount(value.toBoolean())
            UserSettings.SETTING_MAX_ITEM_COUNT_PER_FEED -> settingsStore.setMaxCountPerFeed(value.toIntOrNull() ?: 100)

            // OpenAI related settings
            UserSettings.SETTING_OPENAI_KEY,
            UserSettings.SETTING_OPENAI_MODEL_ID,
            UserSettings.SETTING_OPENAI_URL,
            UserSettings.SETTING_OPENAI_AZURE_VERSION,
            UserSettings.SETTING_OPENAI_AZURE_DEPLOYMENT_ID,
            UserSettings.SETTING_OPENAI_REQUEST_TIMEOUT_SECONDS,
            -> {
                val current = settingsStore.openAiSettings.value
                val newSettings =
                    when (UserSettings.fromKey(key)) {
                        UserSettings.SETTING_OPENAI_KEY -> current.copy(key = value)
                        UserSettings.SETTING_OPENAI_MODEL_ID -> current.copy(modelId = value)
                        UserSettings.SETTING_OPENAI_URL -> current.copy(baseUrl = value)
                        UserSettings.SETTING_OPENAI_AZURE_VERSION -> current.copy(azureApiVersion = value)
                        UserSettings.SETTING_OPENAI_AZURE_DEPLOYMENT_ID -> current.copy(azureDeploymentId = value)
                        UserSettings.SETTING_OPENAI_REQUEST_TIMEOUT_SECONDS -> current.copy(timeoutSeconds = value.toIntOrNull() ?: 30)
                        else -> current
                    }
                settingsStore.setOpenAiSettings(newSettings)
            }
        }
    }

    override suspend fun saveBlocklistPatterns(patterns: Iterable<String>) {
        val existingPatterns = settingsStore.blockListPreference.first()

        patterns
            .asSequence()
            .filterNot { it.isBlank() }
            .filterNot { it in existingPatterns }
            .distinct()
            .forEach {
                settingsStore.addBlocklistPattern(it)
            }
    }
}

private const val LOG_TAG = "FEEDER_OMPLIMPORT"
