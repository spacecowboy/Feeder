package com.nononsenseapps.feeder.model.opml

import com.nononsenseapps.feeder.archmodel.DarkThemePreferences
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.archmodel.ItemOpener
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.archmodel.OpenAISettings
import com.nononsenseapps.feeder.archmodel.PREF_VAL_OPEN_WITH_CUSTOM_TAB
import com.nononsenseapps.feeder.archmodel.SettingsStore
import com.nononsenseapps.feeder.archmodel.SortingOptions
import com.nononsenseapps.feeder.archmodel.SwipeAsRead
import com.nononsenseapps.feeder.archmodel.SyncFrequency
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.archmodel.UserSettings
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.model.OPMLParserHandler
import com.nononsenseapps.feeder.ui.compose.settings.FontSelection
import com.nononsenseapps.feeder.util.FilePathProvider
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

class OpmlParserTest : DIAware {
    private val feedDao: FeedDao = mockk()
    private val settingsStore: SettingsStore = mockk(relaxUnitFun = true)
    private val filePathProvider: FilePathProvider = mockk(relaxUnitFun = true)
    override val di =
        DI.lazy {
            bind<FeedDao>() with instance(feedDao)
            bind<SettingsStore>() with instance(settingsStore)
            bind<OPMLParserHandler>() with singleton { OPMLImporter(di) }
            bind<FilePathProvider>() with singleton { filePathProvider }
        }
    private val opmlImporter: OPMLParserHandler by instance()

    private suspend fun setAllSettings() {
        for (userSetting in UserSettings.entries) {
            opmlImporter.saveSetting(
                key = userSetting.key,
                value =
                    when (userSetting) {
                        UserSettings.SETTING_OPEN_LINKS_WITH -> PREF_VAL_OPEN_WITH_CUSTOM_TAB
                        UserSettings.SETTING_ADDED_FEEDER_NEWS -> "true"
                        UserSettings.SETTING_THEME -> "night"
                        UserSettings.SETTING_DARK_THEME -> "DaRk"
                        UserSettings.SETTING_DYNAMIC_THEME -> "false"
                        UserSettings.SETTING_SORT -> "oldest_first"
                        UserSettings.SETTING_SHOW_FAB -> "false"
                        UserSettings.SETTING_FEED_ITEM_STYLE -> "super_compact"
                        UserSettings.SETTING_SWIPE_AS_READ -> "DISABLED"
                        UserSettings.SETTING_SYNC_ON_RESUME -> "true"
                        UserSettings.SETTING_SYNC_ONLY_WIFI -> "false"
                        UserSettings.SETTING_IMG_ONLY_WIFI -> "true"
                        UserSettings.SETTING_IMG_SHOW_THUMBNAILS -> "false"
                        UserSettings.SETTING_DEFAULT_OPEN_ITEM_WITH -> PREF_VAL_OPEN_WITH_CUSTOM_TAB
                        UserSettings.SETTING_TEXT_SCALE -> "1.6"
                        UserSettings.SETTING_IS_MARK_AS_READ_ON_SCROLL -> "true"
                        UserSettings.SETTING_READALOUD_USE_DETECT_LANGUAGE -> "true"
                        UserSettings.SETTING_SYNC_ONLY_CHARGING -> "true"
                        UserSettings.SETTING_SYNC_FREQ -> "720"
                        UserSettings.SETTING_MAX_LINES -> "6"
                        UserSettings.SETTINGS_FILTER_SAVED -> "true"
                        UserSettings.SETTINGS_FILTER_RECENTLY_READ -> "true"
                        UserSettings.SETTINGS_FILTER_READ -> "false"
                        UserSettings.SETTINGS_LIST_SHOW_ONLY_TITLES -> "true"
                        UserSettings.SETTING_OPEN_ADJACENT -> "true"
                        UserSettings.SETTING_FONT -> "bundled/roboto_flex"
                        UserSettings.SETTING_LIST_SHOW_READING_TIME -> "false"
                        UserSettings.SETTING_OPEN_DRAWER_ON_FAB -> "true"
                        UserSettings.SETTING_SHOW_TITLE_UNREAD_COUNT -> "true"
                        UserSettings.SETTING_MAX_ITEM_COUNT_PER_FEED -> "200"
                        UserSettings.SETTING_OPENAI_KEY -> "test-api-key"
                        UserSettings.SETTING_OPENAI_MODEL_ID -> "gpt-4o-mini"
                        UserSettings.SETTING_OPENAI_URL -> "https://api.openai.com"
                        UserSettings.SETTING_OPENAI_AZURE_VERSION -> "2023-05-15"
                        UserSettings.SETTING_OPENAI_AZURE_DEPLOYMENT_ID -> "test-deployment"
                        UserSettings.SETTING_OPENAI_REQUEST_TIMEOUT_SECONDS -> "45"
                    },
            )
        }
    }

    @Test
    fun handlesAllSettings(): Unit =
        runBlocking {
            every { settingsStore.openAiSettings } returns MutableStateFlow(OpenAISettings())
            every { settingsStore.setOpenAiSettings(any()) } just Runs
            setAllSettings()
            verify {
                settingsStore.setLinkOpener(LinkOpener.CUSTOM_TAB)
                settingsStore.setAddedFeederNews(true)
                settingsStore.setCurrentTheme(ThemeOptions.NIGHT)
                settingsStore.setDarkThemePreference(DarkThemePreferences.DARK)
                settingsStore.setUseDynamicTheme(false)
                settingsStore.setCurrentSorting(SortingOptions.OLDEST_FIRST)
                settingsStore.setShowFab(false)
                settingsStore.setFeedItemStyle(FeedItemStyle.SUPER_COMPACT)
                settingsStore.setSwipeAsRead(SwipeAsRead.DISABLED)
                settingsStore.setSyncOnResume(true)
                settingsStore.setLoadImageOnlyOnWifi(true)
                settingsStore.setShowThumbnails(false)
                settingsStore.setItemOpener(ItemOpener.CUSTOM_TAB)
                settingsStore.setTextScale(1.6f)
                settingsStore.setIsMarkAsReadOnScroll(true)
                settingsStore.setUseDetectLanguage(true)
                settingsStore.setSyncOnlyWhenCharging(true)
                settingsStore.setSyncOnlyOnWifi(false)
                settingsStore.setSyncFrequency(SyncFrequency.EVERY_12_HOURS)
                settingsStore.setMaxLines(6)
                settingsStore.setFeedListFilterRecentlyRead(true)
                settingsStore.setFeedListFilterRead(false)
                settingsStore.setFeedListFilterSaved(true)
                settingsStore.setShowOnlyTitles(true)
                settingsStore.setOpenAdjacent(true)
                settingsStore.setFont(FontSelection.RobotoFlex)
                settingsStore.setShowReadingTime(false)
                settingsStore.setOpenDrawerOnFab(true)
                settingsStore.setShowTitleUnreadCount(true)
                settingsStore.setMaxCountPerFeed(200)
                settingsStore.openAiSettings
                settingsStore.setOpenAiSettings(any())
//                settingsStore.setOpenAiSettings(
//                    OpenAISettings(
//                        modelId = "gpt-4o-mini",
//                        baseUrl = "https://api.openai.com",
//                        timeoutSeconds = 45,
//                        azureApiVersion = "2023-05-15",
//                        azureDeploymentId = "test-deployment",
//                        key = "test-api-key",
//                    )
//                )
            }

            confirmVerified(settingsStore)
        }

    @Test
    fun handlesBlockedPatterns(): Unit =
        runBlocking {
            every { settingsStore.blockListPreference } returns
                flowOf(
                    listOf("existing"),
                )

            opmlImporter.saveBlocklistPatterns(
                listOf(
                    "foo",
                    "existing",
                    "foo",
                    "injection break';",
                    "",
                    " ",
                ),
            )

            verify(exactly = 1) {
                settingsStore.blockListPreference
            }

            coVerify(exactly = 1) {
                settingsStore.addBlocklistPattern("foo")
                settingsStore.addBlocklistPattern("injection break';")
            }

            confirmVerified(settingsStore)
        }
}
