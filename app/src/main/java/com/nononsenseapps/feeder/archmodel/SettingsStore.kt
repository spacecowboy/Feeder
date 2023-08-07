package com.nononsenseapps.feeder.archmodel

import android.content.SharedPreferences
import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import androidx.annotation.StringRes
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.BlocklistDao
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.workmanager.FeedSyncer
import com.nononsenseapps.feeder.model.workmanager.UNIQUE_PERIODIC_NAME
import com.nononsenseapps.feeder.model.workmanager.oldPeriodics
import com.nononsenseapps.feeder.util.PREF_MAX_ITEM_COUNT_PER_FEED
import com.nononsenseapps.feeder.util.getStringNonNull
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsStore(override val di: DI) : DIAware {
    private val sp: SharedPreferences by instance()
    private val blocklistDao: BlocklistDao by instance()

    private val _addedFeederNews = MutableStateFlow(sp.getBoolean(PREF_ADDED_FEEDER_NEWS, false))
    val addedFeederNews: StateFlow<Boolean> = _addedFeederNews.asStateFlow()
    fun setAddedFeederNews(value: Boolean) {
        sp.edit().putBoolean(PREF_ADDED_FEEDER_NEWS, value).apply()
        _addedFeederNews.value = value
    }

    private val _showOnlyUnread = MutableStateFlow(sp.getBoolean(PREF_SHOW_ONLY_UNREAD, true))
    val showOnlyUnread: StateFlow<Boolean> = _showOnlyUnread.asStateFlow()
    fun setShowOnlyUnread(value: Boolean) {
        sp.edit().putBoolean(PREF_SHOW_ONLY_UNREAD, value).apply()
        _showOnlyUnread.value = value
        _minReadTime.value = when (value) {
            true -> Instant.now()
            false -> Instant.EPOCH
        }
    }

    private val _minReadTime: MutableStateFlow<Instant> = MutableStateFlow(
        // by design - min read time is never written to disk
        when (_showOnlyUnread.value) {
            true -> Instant.now()
            false -> Instant.EPOCH
        },
    )
    val minReadTime: StateFlow<Instant> = _minReadTime.asStateFlow()
    fun setMinReadTime(value: Instant) {
        if (showOnlyUnread.value) {
            _minReadTime.value = value
        }
    }

    private val _currentFeedAndTag = MutableStateFlow(
        sp.getLong(PREF_LAST_FEED_ID, ID_UNSET) to (sp.getString(PREF_LAST_FEED_TAG, null) ?: ""),
    )
    val currentFeedAndTag = _currentFeedAndTag.asStateFlow()

    /**
     * Returns true if the parameters were different from current state
     */
    fun setCurrentFeedAndTag(feedId: Long, tag: String): Boolean =
        if (_currentFeedAndTag.value.first != feedId ||
            _currentFeedAndTag.value.second != tag
        ) {
            _currentFeedAndTag.value = feedId to tag
            sp.edit().putLong(PREF_LAST_FEED_ID, feedId).apply()
            sp.edit().putString(PREF_LAST_FEED_TAG, tag).apply()
            true
        } else {
            false
        }

    private val _currentArticle = MutableStateFlow(
        sp.getLong(PREF_LAST_ARTICLE_ID, ID_UNSET),
    )
    val currentArticleId = _currentArticle.asStateFlow()
    fun setCurrentArticle(articleId: Long) {
        _currentArticle.value = articleId
        sp.edit().putLong(PREF_LAST_ARTICLE_ID, articleId).apply()
    }

    private val _isArticleOpen = MutableStateFlow(
        sp.getBoolean(PREF_IS_ARTICLE_OPEN, false),
    )
    val isArticleOpen: StateFlow<Boolean> = _isArticleOpen.asStateFlow()
    fun setIsArticleOpen(open: Boolean) {
        _isArticleOpen.update { open }
        sp.edit().putBoolean(PREF_IS_ARTICLE_OPEN, open).apply()
    }

    private val _isMarkAsReadOnScroll = MutableStateFlow(
        sp.getBoolean(PREF_IS_MARK_AS_READ_ON_SCROLL, false),
    )
    val isMarkAsReadOnScroll: StateFlow<Boolean> = _isMarkAsReadOnScroll.asStateFlow()
    fun setIsMarkAsReadOnScroll(open: Boolean) {
        _isMarkAsReadOnScroll.update { open }
        sp.edit().putBoolean(PREF_IS_MARK_AS_READ_ON_SCROLL, open).apply()
    }

    private val _currentTheme = MutableStateFlow(
        themeOptionsFromString(
            sp.getString(PREF_THEME, null)?.uppercase()
                ?: ThemeOptions.SYSTEM.name,
        ),
    )
    val currentTheme = _currentTheme.asStateFlow()
    fun setCurrentTheme(value: ThemeOptions) {
        _currentTheme.value = value
        sp.edit().putString(PREF_THEME, value.name.lowercase()).apply()
    }

    private val _darkThemePreference = MutableStateFlow(
        darkThemePreferenceFromString(
            sp.getString(PREF_DARK_THEME, null)?.uppercase()
                ?: DarkThemePreferences.BLACK.name,
        ),
    )
    val darkThemePreference = _darkThemePreference.asStateFlow()
    fun setDarkThemePreference(value: DarkThemePreferences) {
        _darkThemePreference.value = value
        sp.edit().putString(PREF_DARK_THEME, value.name.lowercase()).apply()
    }

    private val _currentSorting = MutableStateFlow(
        sortingOptionsFromString(
            sp.getString(PREF_SORT, null)?.uppercase()
                ?: SortingOptions.NEWEST_FIRST.name,
        ),
    )
    val currentSorting = _currentSorting.asStateFlow()
    fun setCurrentSorting(value: SortingOptions) {
        _currentSorting.value = value
        sp.edit().putString(PREF_SORT, value.name.lowercase()).apply()
    }

    private val _showFab = MutableStateFlow(sp.getBoolean(PREF_SHOW_FAB, true))
    val showFab = _showFab.asStateFlow()
    fun setShowFab(value: Boolean) {
        _showFab.value = value
        sp.edit().putBoolean(PREF_SHOW_FAB, value).apply()
    }

    private val _syncOnResume = MutableStateFlow(sp.getBoolean(PREF_SYNC_ON_RESUME, false))
    val syncOnResume = _syncOnResume.asStateFlow()
    fun setSyncOnResume(value: Boolean) {
        _syncOnResume.value = value
        sp.edit().putBoolean(PREF_SYNC_ON_RESUME, value).apply()
    }

    private val _syncOnlyOnWifi = MutableStateFlow(sp.getBoolean(PREF_SYNC_ONLY_WIFI, false))
    val syncOnlyOnWifi = _syncOnlyOnWifi.asStateFlow()
    fun setSyncOnlyOnWifi(value: Boolean) {
        _syncOnlyOnWifi.value = value
        sp.edit().putBoolean(PREF_SYNC_ONLY_WIFI, value).apply()
        configurePeriodicSync(replace = true)
    }

    private val _syncOnlyWhenCharging =
        MutableStateFlow(sp.getBoolean(PREF_SYNC_ONLY_CHARGING, false))
    val syncOnlyWhenCharging = _syncOnlyWhenCharging.asStateFlow()
    fun setSyncOnlyWhenCharging(value: Boolean) {
        _syncOnlyWhenCharging.value = value
        sp.edit().putBoolean(PREF_SYNC_ONLY_CHARGING, value).apply()
        configurePeriodicSync(replace = true)
    }

    private val _loadImageOnlyOnWifi = MutableStateFlow(sp.getBoolean(PREF_IMG_ONLY_WIFI, false))
    val loadImageOnlyOnWifi = _loadImageOnlyOnWifi.asStateFlow()
    fun setLoadImageOnlyOnWifi(value: Boolean) {
        _loadImageOnlyOnWifi.value = value
        sp.edit().putBoolean(PREF_IMG_ONLY_WIFI, value).apply()
    }

    private val _showThumbnails = MutableStateFlow(sp.getBoolean(PREF_IMG_SHOW_THUMBNAILS, true))
    val showThumbnails = _showThumbnails.asStateFlow()
    fun setShowThumbnails(value: Boolean) {
        _showThumbnails.value = value
        sp.edit().putBoolean(PREF_IMG_SHOW_THUMBNAILS, value).apply()
    }

    private val _useDetectLanguage =
        MutableStateFlow(sp.getBoolean(PREF_READALOUD_USE_DETECT_LANGUAGE, true))
    val useDetectLanguage = _useDetectLanguage.asStateFlow()
    fun setUseDetectLanguage(value: Boolean) {
        _useDetectLanguage.value = value
        sp.edit().putBoolean(PREF_READALOUD_USE_DETECT_LANGUAGE, value).apply()
    }

    private val _useDynamicTheme =
        MutableStateFlow(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && sp.getBoolean(
                PREF_DYNAMIC_THEME,
                true,
            ),
        )
    val useDynamicTheme = _useDynamicTheme.asStateFlow()
    fun setUseDynamicTheme(value: Boolean) {
        _useDynamicTheme.value = value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        sp.edit().putBoolean(PREF_DYNAMIC_THEME, value).apply()
    }

    private val _textScale =
        MutableStateFlow(
            sp.getFloat(
                PREF_TEXT_SCALE,
                1f,
            ),
        )
    val textScale = _textScale.asStateFlow()
    fun setTextScale(value: Float) {
        _textScale.value = value
        sp.edit().putFloat(PREF_TEXT_SCALE, value).apply()
    }

    private val _maximumCountPerFeed =
        MutableStateFlow(sp.getStringNonNull(PREF_MAX_ITEM_COUNT_PER_FEED, "100").toInt())
    val maximumCountPerFeed = _maximumCountPerFeed.asStateFlow()
    fun setMaxCountPerFeed(value: Int) {
        _maximumCountPerFeed.value = value
        sp.edit().putString(PREF_MAX_ITEM_COUNT_PER_FEED, "$value").apply()
    }

    private val _itemOpener = MutableStateFlow(
        itemOpenerFromString(
            sp.getStringNonNull(
                PREF_DEFAULT_OPEN_ITEM_WITH,
                PREF_VAL_OPEN_WITH_READER,
            ),
        ),
    )
    val itemOpener = _itemOpener.asStateFlow()
    fun setItemOpener(value: ItemOpener) {
        _itemOpener.value = value
        sp.edit().putString(
            PREF_DEFAULT_OPEN_ITEM_WITH,
            when (value) {
                ItemOpener.READER -> PREF_VAL_OPEN_WITH_READER
                ItemOpener.CUSTOM_TAB -> PREF_VAL_OPEN_WITH_CUSTOM_TAB
                ItemOpener.DEFAULT_BROWSER -> PREF_VAL_OPEN_WITH_BROWSER
            },
        ).apply()
    }

    private val _linkOpener = MutableStateFlow(
        linkOpenerFromString(
            sp.getStringNonNull(PREF_OPEN_LINKS_WITH, PREF_VAL_OPEN_WITH_CUSTOM_TAB),
        ),
    )
    val linkOpener = _linkOpener.asStateFlow()
    fun setLinkOpener(value: LinkOpener) {
        _linkOpener.value = value
        sp.edit().putString(
            PREF_OPEN_LINKS_WITH,
            when (value) {
                LinkOpener.CUSTOM_TAB -> PREF_VAL_OPEN_WITH_CUSTOM_TAB
                LinkOpener.DEFAULT_BROWSER -> PREF_VAL_OPEN_WITH_BROWSER
            },
        ).apply()
    }

    private val _feedItemStyle = MutableStateFlow(
        feedItemStyleFromString(sp.getStringNonNull(PREF_FEED_ITEM_STYLE, FeedItemStyle.CARD.name)),
    )
    val feedItemStyle = _feedItemStyle.asStateFlow()
    fun setFeedItemStyle(value: FeedItemStyle) {
        _feedItemStyle.value = value
        sp.edit().putString(
            PREF_FEED_ITEM_STYLE,
            value.name,
        ).apply()
    }

    private val _swipeAsRead = MutableStateFlow(
        swipeAsReadFromString(
            sp.getStringNonNull(PREF_SWIPE_AS_READ, SwipeAsRead.ONLY_FROM_END.name),
        ),
    )
    val swipeAsRead = _swipeAsRead.asStateFlow()
    fun setSwipeAsRead(value: SwipeAsRead) {
        _swipeAsRead.value = value
        sp.edit().putString(
            PREF_SWIPE_AS_READ,
            value.name,
        ).apply()
    }

    val blockListPreference: Flow<List<String>>
        get() = blocklistDao.getGlobPatterns()
            .mapLatest { patterns ->
                patterns.map { pattern ->
                    // Remove start and ending *
                    pattern.dropEnds(1, 1)
                }
            }

    suspend fun removeBlocklistPattern(value: String) {
        blocklistDao.deletePattern(value)
    }

    suspend fun addBlocklistPattern(value: String) {
        if (value.isBlank()) {
            return
        }
        try {
            blocklistDao.insertSafely(value.lowercase().trim())
        } catch (e: SQLiteConstraintException) {
            // Ignore: Query style method can't define ignore on constraint failures
        }
    }

    private val _syncFrequency by lazy {
        MutableStateFlow(
            syncFrequencyFromString(sp.getStringNonNull(PREF_SYNC_FREQ, "60")),
        )
    }
    val syncFrequency = _syncFrequency.asStateFlow()
    fun setSyncFrequency(value: SyncFrequency) {
        _syncFrequency.value = value
        sp.edit().putString(PREF_SYNC_FREQ, "${value.minutes}").apply()
        configurePeriodicSync(replace = true)
    }

    fun configurePeriodicSync(replace: Boolean) {
        val workManager: WorkManager by instance()
        val shouldSync = syncFrequency.value.minutes > 0

        // Clear old job always to replace with new one
        for (oldPeriodic in oldPeriodics) {
            workManager.cancelUniqueWork(oldPeriodic)
        }

        if (shouldSync) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(syncOnlyWhenCharging.value)

            if (syncOnlyOnWifi.value) {
                constraints.setRequiredNetworkType(NetworkType.UNMETERED)
            } else {
                constraints.setRequiredNetworkType(NetworkType.CONNECTED)
            }

            val timeInterval = syncFrequency.value.minutes

            val workRequestBuilder = PeriodicWorkRequestBuilder<FeedSyncer>(
                timeInterval,
                TimeUnit.MINUTES,
            )

            val syncWork = workRequestBuilder
                .setConstraints(constraints.build())
                .addTag("feeder")
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_NAME,
                when (replace) {
                    true -> ExistingPeriodicWorkPolicy.UPDATE
                    false -> ExistingPeriodicWorkPolicy.KEEP
                },
                syncWork,
            )
        } else {
            workManager.cancelUniqueWork(UNIQUE_PERIODIC_NAME)
        }
    }

    fun getAllSettings(): Map<String, String> {
        val all = sp.all ?: emptyMap()

        val userPrefs = UserSettings.values().mapTo(mutableSetOf()) { it.key }

        return all.filterKeys { it in userPrefs }
            .mapValues { (_, value) -> value.toString() }
    }

    companion object {
//        private const val LOG_TAG = "FEEDER_SETTINGSSTORE"
    }
}

/**
 * Boolean indicating if only unread items should be shown
 */
const val PREF_SHOW_ONLY_UNREAD = "pref_show_only_unread"

/**
 * Boolean indicating if Feeder News feed has been added or not
 */
const val PREF_ADDED_FEEDER_NEWS = "pref_added_feeder_news"

/**
 * These indicate which fragment to open by default
 */
const val PREF_LAST_FEED_TAG = "pref_last_feed_tag"
const val PREF_LAST_FEED_ID = "pref_last_feed_id"
const val PREF_LAST_ARTICLE_ID = "pref_last_article_id"
const val PREF_IS_ARTICLE_OPEN = "pref_is_article_open"

/**
 * Theme settings
 */
const val PREF_THEME = "pref_theme"

/**
 * Dark theme settings
 */
const val PREF_DARK_THEME = "pref_dark_theme"
const val PREF_DYNAMIC_THEME = "pref_dynamic_theme"

/**
 * Sort settings
 */
const val PREF_SORT = "pref_sort"

/**
 * Floating action button settings
 */
const val PREF_SHOW_FAB = "pref_show_fab"

const val PREF_FEED_ITEM_STYLE = "pref_feed_item_style"

const val PREF_SWIPE_AS_READ = "pref_swipe_as_read"

/**
 * Sync settings
 */
const val PREF_SYNC_ONLY_CHARGING = "pref_sync_only_charging"
const val PREF_SYNC_ONLY_WIFI = "pref_sync_only_wifi"
const val PREF_SYNC_FREQ = "pref_sync_freq"
const val PREF_SYNC_ON_RESUME = "pref_sync_on_resume"

/**
 * Image settings
 */
const val PREF_IMG_ONLY_WIFI = "pref_img_only_wifi"
const val PREF_IMG_SHOW_THUMBNAILS = "pref_img_show_thumbnails"

/**
 * Reader settings
 */
const val PREF_DEFAULT_OPEN_ITEM_WITH = "pref_default_open_item_with"
const val PREF_OPEN_LINKS_WITH = "pref_open_links_with"

const val PREF_VAL_OPEN_WITH_READER = "0"
const val PREF_VAL_OPEN_WITH_WEBVIEW = "1"
const val PREF_VAL_OPEN_WITH_BROWSER = "2"
const val PREF_VAL_OPEN_WITH_CUSTOM_TAB = "3"

const val PREF_TEXT_SCALE = "pref_body_text_scale"

const val PREF_IS_MARK_AS_READ_ON_SCROLL = "pref_is_mark_as_read_on_scroll"

/**
 * Read Aloud Settings
 */
const val PREF_READALOUD_USE_DETECT_LANGUAGE = "pref_readaloud_detect_lang"

enum class SettingType {
    STRING,
    FLOAT,
    BOOLEAN,
}

/**
 * Used for OPML Import/Export. Please add new (only) user configurable settings here
 */
enum class UserSettings(val key: String, val type: SettingType) {
    SETTING_SHOW_ONLY_UNREAD(key = PREF_SHOW_ONLY_UNREAD, type = SettingType.BOOLEAN),
    SETTING_ADDED_FEEDER_NEWS(key = PREF_ADDED_FEEDER_NEWS, type = SettingType.BOOLEAN),
    SETTING_THEME(key = PREF_THEME, type = SettingType.STRING),
    SETTING_DARK_THEME(key = PREF_DARK_THEME, type = SettingType.STRING),
    SETTING_DYNAMIC_THEME(key = PREF_DYNAMIC_THEME, type = SettingType.BOOLEAN),
    SETTING_SORT(key = PREF_SORT, type = SettingType.STRING),
    SETTING_SHOW_FAB(key = PREF_SHOW_FAB, type = SettingType.BOOLEAN),
    SETTING_FEED_ITEM_STYLE(key = PREF_FEED_ITEM_STYLE, type = SettingType.STRING),
    SETTING_SWIPE_AS_READ(key = PREF_SWIPE_AS_READ, type = SettingType.STRING),
    SETTING_SYNC_ONLY_CHARGING(key = PREF_SYNC_ONLY_CHARGING, type = SettingType.BOOLEAN),
    SETTING_SYNC_ONLY_WIFI(key = PREF_SYNC_ONLY_WIFI, type = SettingType.BOOLEAN),
    SETTING_SYNC_FREQ(key = PREF_SYNC_FREQ, type = SettingType.STRING),
    SETTING_SYNC_ON_RESUME(key = PREF_SYNC_ON_RESUME, type = SettingType.BOOLEAN),
    SETTING_IMG_ONLY_WIFI(key = PREF_IMG_ONLY_WIFI, type = SettingType.BOOLEAN),
    SETTING_IMG_SHOW_THUMBNAILS(key = PREF_IMG_SHOW_THUMBNAILS, type = SettingType.BOOLEAN),
    SETTING_DEFAULT_OPEN_ITEM_WITH(key = PREF_DEFAULT_OPEN_ITEM_WITH, type = SettingType.STRING),
    SETTING_OPEN_LINKS_WITH(key = PREF_OPEN_LINKS_WITH, type = SettingType.STRING),
    SETTING_TEXT_SCALE(key = PREF_TEXT_SCALE, type = SettingType.FLOAT),
    SETTING_IS_MARK_AS_READ_ON_SCROLL(
        key = PREF_IS_MARK_AS_READ_ON_SCROLL,
        type = SettingType.BOOLEAN,
    ),
    SETTING_READALOUD_USE_DETECT_LANGUAGE(
        key = PREF_READALOUD_USE_DETECT_LANGUAGE,
        type = SettingType.BOOLEAN,
    ),
    ;

    companion object {
        fun fromKey(key: String): UserSettings? {
            return values().firstOrNull { it.key.equals(key, ignoreCase = true) }
        }
    }
}

enum class ThemeOptions(
    @StringRes val stringId: Int,
) {
    DAY(R.string.theme_day),
    NIGHT(R.string.theme_night),
    SYSTEM(R.string.theme_system),
}

enum class DarkThemePreferences(
    @StringRes val stringId: Int,
) {
    BLACK(R.string.dark_theme_preference_black),
    DARK(R.string.dark_theme_preference_dark),
}

enum class SortingOptions(
    @StringRes val stringId: Int,
) {
    NEWEST_FIRST(R.string.sort_newest_first),
    OLDEST_FIRST(R.string.sort_oldest_first),
}

enum class ItemOpener(
    @StringRes val stringId: Int,
) {
    READER(R.string.open_in_reader),
    CUSTOM_TAB(R.string.open_in_custom_tab),
    DEFAULT_BROWSER(R.string.open_in_default_browser),
}

enum class LinkOpener(
    @StringRes val stringId: Int,
) {
    CUSTOM_TAB(R.string.open_in_custom_tab),
    DEFAULT_BROWSER(R.string.open_in_default_browser),
}

enum class SyncFrequency(
    val minutes: Long,
    @StringRes val stringId: Int,
) {
    MANUAL(-1L, R.string.sync_option_manually),
    EVERY_15_MIN(15L, R.string.sync_option_every_15min),
    EVERY_30_MIN(30L, R.string.sync_option_every_30min),
    EVERY_1_HOURS(60L, R.string.sync_option_every_hour),
    EVERY_3_HOURS(180L, R.string.sync_option_every_3_hours),
    EVERY_6_HOURS(360L, R.string.sync_option_every_6_hours),
    EVERY_12_HOURS(720L, R.string.sync_option_every_12_hours),
    EVERY_DAY(1440L, R.string.sync_option_every_day),
}

enum class FeedItemStyle(
    @StringRes val stringId: Int,
) {
    CARD(R.string.feed_item_style_card),
    COMPACT(R.string.feed_item_style_compact),
    SUPER_COMPACT(R.string.feed_item_style_super_compact),
}

enum class SwipeAsRead(
    @StringRes val stringId: Int,
) {
    DISABLED(R.string.disabled),
    ONLY_FROM_END(R.string.only_from_end),
    FROM_ANYWHERE(R.string.from_anywhere),
}

fun String.dropEnds(
    starting: Int,
    ending: Int,
): String {
    require(starting >= 0) { "Requested character count $starting is less than zero." }
    require(ending >= 0) { "Requested character count $ending is less than zero." }
    return substring(
        starting.coerceAtMost(length),
        (length - ending).coerceAtLeast(0),
    )
}

fun linkOpenerFromString(value: String): LinkOpener = when (value) {
    PREF_VAL_OPEN_WITH_BROWSER -> LinkOpener.DEFAULT_BROWSER
    else -> LinkOpener.CUSTOM_TAB
}

fun itemOpenerFromString(value: String) = when (value) {
    PREF_VAL_OPEN_WITH_BROWSER -> ItemOpener.DEFAULT_BROWSER
    PREF_VAL_OPEN_WITH_WEBVIEW,
    PREF_VAL_OPEN_WITH_CUSTOM_TAB,
    -> ItemOpener.CUSTOM_TAB

    else -> ItemOpener.READER
}

fun sortingOptionsFromString(value: String): SortingOptions = try {
    SortingOptions.valueOf(value.uppercase())
} catch (_: Exception) {
    SortingOptions.NEWEST_FIRST
}

fun themeOptionsFromString(value: String) = try {
    ThemeOptions.valueOf(value.uppercase())
} catch (_: Exception) {
    ThemeOptions.SYSTEM
}

fun darkThemePreferenceFromString(value: String): DarkThemePreferences = try {
    DarkThemePreferences.valueOf(value.uppercase())
} catch (_: Exception) {
    DarkThemePreferences.BLACK
}

fun swipeAsReadFromString(value: String): SwipeAsRead = try {
    SwipeAsRead.valueOf(value.uppercase())
} catch (_: Exception) {
    SwipeAsRead.ONLY_FROM_END
}

fun feedItemStyleFromString(value: String) = try {
    FeedItemStyle.valueOf(value.uppercase())
} catch (_: Exception) {
    FeedItemStyle.CARD
}

fun syncFrequencyFromString(value: String) =
    SyncFrequency.values()
        .firstOrNull {
            it.minutes == value.toLongOrNull()
        }
        ?: SyncFrequency.MANUAL
