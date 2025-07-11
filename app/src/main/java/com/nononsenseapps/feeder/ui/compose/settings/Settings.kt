package com.nononsenseapps.feeder.ui.compose.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices.NEXUS_5
import androidx.compose.ui.tooling.preview.Devices.PIXEL_C
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.shouldShowRationale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.DarkThemePreferences
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.archmodel.ItemOpener
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.archmodel.SortingOptions
import com.nononsenseapps.feeder.archmodel.SwipeAsRead
import com.nononsenseapps.feeder.archmodel.SyncFrequency
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.dialog.EditableListDialog
import com.nononsenseapps.feeder.ui.compose.dialog.FeedNotificationsDialog
import com.nononsenseapps.feeder.ui.compose.feed.ExplainPermissionDialog
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.LocalTypographySettings
import com.nononsenseapps.feeder.ui.compose.theme.SensibleTopAppBar
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.WithAllPreviewProviders
import com.nononsenseapps.feeder.ui.compose.utils.immutableListHolderOf
import com.nononsenseapps.feeder.ui.compose.utils.onKeyEventLikeEscape
import com.nononsenseapps.feeder.ui.compose.utils.rememberApiPermissionState
import com.nononsenseapps.feeder.util.ActivityLauncher
import org.kodein.di.compose.LocalDI
import org.kodein.di.instance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToSyncScreen: () -> Unit,
    onNavigateToTextSettingsScreen: () -> Unit,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val viewState by settingsViewModel.viewState.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val activityLauncher: ActivityLauncher by LocalDI.current.instance()

    Scaffold(
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            SensibleTopAppBar(
                scrollBehavior = scrollBehavior,
                title = stringResource(id = R.string.title_activity_settings),
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        SettingsList(
            currentThemeValue = viewState.currentTheme.asThemeOption(),
            onThemeChange = { value ->
                settingsViewModel.setCurrentTheme(value.currentTheme)
            },
            currentDarkThemePreference = viewState.darkThemePreference.asDarkThemeOption(),
            onDarkThemePreferenceChange = { value ->
                settingsViewModel.setPreferredDarkTheme(value.darkThemePreferences)
            },
            currentSortingValue = viewState.currentSorting.asSortOption(),
            onSortingChange = { value ->
                settingsViewModel.setCurrentSorting(value.currentSorting)
            },
            showFabValue = viewState.showFab,
            onShowFabChange = settingsViewModel::setShowFab,
            feedItemStyleValue = viewState.feedItemStyle,
            onFeedItemStyleChange = settingsViewModel::setFeedItemStyle,
            blockListValue = ImmutableHolder(viewState.blockList.sorted()),
            swipeAsReadValue = viewState.swipeAsRead,
            onSwipeAsReadOptionChange = settingsViewModel::setSwipeAsRead,
            syncOnStartupValue = viewState.syncOnResume,
            onSyncOnStartupChange = settingsViewModel::setSyncOnResume,
            syncOnlyOnWifiValue = viewState.syncOnlyOnWifi,
            onSyncOnlyOnWifiChange = settingsViewModel::setSyncOnlyOnWifi,
            syncOnlyWhenChargingValue = viewState.syncOnlyWhenCharging,
            onSyncOnlyWhenChargingChange = settingsViewModel::setSyncOnlyWhenCharging,
            loadImageOnlyOnWifiValue = viewState.loadImageOnlyOnWifi,
            onLoadImageOnlyOnWifiChange = settingsViewModel::setLoadImageOnlyOnWifi,
            showThumbnailsValue = viewState.showThumbnails,
            onShowThumbnailsChange = settingsViewModel::setShowThumbnails,
            useDetectLanguage = viewState.useDetectLanguage,
            onUseDetectLanguageChange = settingsViewModel::setUseDetectLanguage,
            maxItemsPerFeedValue = viewState.maximumCountPerFeed,
            onMaxItemsPerFeedChange = settingsViewModel::setMaxCountPerFeed,
            currentItemOpenerValue = viewState.itemOpener,
            onItemOpenerChange = settingsViewModel::setItemOpener,
            currentLinkOpenerValue = viewState.linkOpener,
            onLinkOpenerChange = settingsViewModel::setLinkOpener,
            currentSyncFrequencyValue = viewState.syncFrequency,
            onSyncFrequencyChange = settingsViewModel::setSyncFrequency,
            batteryOptimizationIgnoredValue = viewState.batteryOptimizationIgnored,
            onOpenSyncSettings = onNavigateToSyncScreen,
            useDynamicTheme = viewState.useDynamicTheme,
            onUseDynamicTheme = settingsViewModel::setUseDynamicTheme,
            onBlockListAdd = settingsViewModel::addToBlockList,
            onBlockListRemove = settingsViewModel::removeFromBlockList,
            feedsSettings = ImmutableHolder(viewState.feedsSettings),
            onToggleNotification = settingsViewModel::toggleNotifications,
            isMarkAsReadOnScroll = viewState.isMarkAsReadOnScroll,
            onMarkAsReadOnScroll = settingsViewModel::setIsMarkAsReadOnScroll,
            maxLines = viewState.maxLines,
            setMaxLines = settingsViewModel::setMaxLines,
            showOnlyTitle = viewState.showOnlyTitle,
            onShowOnlyTitle = settingsViewModel::setShowOnlyTitles,
            isOpenAdjacent = viewState.isOpenAdjacent,
            onOpenAdjacent = settingsViewModel::setIsOpenAdjacent,
            showReadingTime = viewState.showReadingTime,
            onShowReadingTimeChange = settingsViewModel::setShowReadingTime,
            showTitleUnreadCount = viewState.showTitleUnreadCount,
            onShowTitleUnreadCountChange = settingsViewModel::setShowTitleUnreadCount,
            onStartActivity = { intent ->
                activityLauncher.startActivity(false, intent)
            },
            openAIState = viewState.openAIState,
            onOpenAIEvent = settingsViewModel::onOpenAISettingsEvent,
            isOpenDrawerOnFab = viewState.isOpenDrawerOnFab,
            onOpenDrawerOnFab = settingsViewModel::setOpenDrawerOnFab,
            onTextSettings = onNavigateToTextSettingsScreen,
            currentFontSelection = viewState.font,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
@Preview(showBackground = true, device = PIXEL_C)
@Preview(showBackground = true, device = NEXUS_5)
private fun SettingsScreenPreview() {
    WithAllPreviewProviders {
        SettingsList(
            currentThemeValue = ThemeOptions.SYSTEM.asThemeOption(),
            onThemeChange = {},
            currentDarkThemePreference = DarkThemePreferences.BLACK.asDarkThemeOption(),
            onDarkThemePreferenceChange = {},
            currentSortingValue = SortingOptions.NEWEST_FIRST.asSortOption(),
            onSortingChange = {},
            showFabValue = true,
            onShowFabChange = {},
            feedItemStyleValue = FeedItemStyle.CARD,
            onFeedItemStyleChange = {},
            blockListValue = ImmutableHolder(emptyList()),
            swipeAsReadValue = SwipeAsRead.ONLY_FROM_END,
            onSwipeAsReadOptionChange = {},
            syncOnStartupValue = true,
            onSyncOnStartupChange = {},
            syncOnlyOnWifiValue = true,
            onSyncOnlyOnWifiChange = {},
            syncOnlyWhenChargingValue = true,
            onSyncOnlyWhenChargingChange = {},
            loadImageOnlyOnWifiValue = true,
            onLoadImageOnlyOnWifiChange = {},
            showThumbnailsValue = true,
            onShowThumbnailsChange = {},
            useDetectLanguage = true,
            onUseDetectLanguageChange = {},
            maxItemsPerFeedValue = 101,
            onMaxItemsPerFeedChange = {},
            currentItemOpenerValue = ItemOpener.CUSTOM_TAB,
            onItemOpenerChange = {},
            currentLinkOpenerValue = LinkOpener.DEFAULT_BROWSER,
            onLinkOpenerChange = {},
            currentSyncFrequencyValue = SyncFrequency.EVERY_12_HOURS,
            onSyncFrequencyChange = {},
            batteryOptimizationIgnoredValue = false,
            onOpenSyncSettings = {},
            useDynamicTheme = true,
            onUseDynamicTheme = {},
            onBlockListAdd = {},
            onBlockListRemove = {},
            feedsSettings = ImmutableHolder(emptyList()),
            onToggleNotification = { _, _ -> },
            isMarkAsReadOnScroll = false,
            onMarkAsReadOnScroll = {},
            maxLines = 2,
            setMaxLines = {},
            showOnlyTitle = false,
            onShowOnlyTitle = {},
            isOpenAdjacent = false,
            onOpenAdjacent = {},
            showReadingTime = false,
            onShowReadingTimeChange = {},
            showTitleUnreadCount = false,
            onShowTitleUnreadCountChange = {},
            onStartActivity = {},
            openAIState = OpenAISettingsState(),
            onOpenAIEvent = {},
            isOpenDrawerOnFab = false,
            onOpenDrawerOnFab = {},
            onTextSettings = {},
            currentFontSelection = FontSelection.SystemDefault,
            modifier = Modifier,
        )
    }
}

@Composable
fun SettingsList(
    currentThemeValue: ThemeOption,
    onThemeChange: (ThemeOption) -> Unit,
    currentDarkThemePreference: DarkThemeOption,
    onDarkThemePreferenceChange: (DarkThemeOption) -> Unit,
    currentSortingValue: SortOption,
    onSortingChange: (SortOption) -> Unit,
    showFabValue: Boolean,
    onShowFabChange: (Boolean) -> Unit,
    feedItemStyleValue: FeedItemStyle,
    onFeedItemStyleChange: (FeedItemStyle) -> Unit,
    blockListValue: ImmutableHolder<List<String>>,
    swipeAsReadValue: SwipeAsRead,
    onSwipeAsReadOptionChange: (SwipeAsRead) -> Unit,
    syncOnStartupValue: Boolean,
    onSyncOnStartupChange: (Boolean) -> Unit,
    syncOnlyOnWifiValue: Boolean,
    onSyncOnlyOnWifiChange: (Boolean) -> Unit,
    syncOnlyWhenChargingValue: Boolean,
    onSyncOnlyWhenChargingChange: (Boolean) -> Unit,
    loadImageOnlyOnWifiValue: Boolean,
    onLoadImageOnlyOnWifiChange: (Boolean) -> Unit,
    showThumbnailsValue: Boolean,
    onShowThumbnailsChange: (Boolean) -> Unit,
    useDetectLanguage: Boolean,
    onUseDetectLanguageChange: (Boolean) -> Unit,
    maxItemsPerFeedValue: Int,
    onMaxItemsPerFeedChange: (Int) -> Unit,
    currentItemOpenerValue: ItemOpener,
    onItemOpenerChange: (ItemOpener) -> Unit,
    currentLinkOpenerValue: LinkOpener,
    onLinkOpenerChange: (LinkOpener) -> Unit,
    currentSyncFrequencyValue: SyncFrequency,
    onSyncFrequencyChange: (SyncFrequency) -> Unit,
    batteryOptimizationIgnoredValue: Boolean,
    onOpenSyncSettings: () -> Unit,
    useDynamicTheme: Boolean,
    onUseDynamicTheme: (Boolean) -> Unit,
    onBlockListAdd: (String) -> Unit,
    onBlockListRemove: (String) -> Unit,
    feedsSettings: ImmutableHolder<List<UIFeedSettings>>,
    onToggleNotification: (Long, Boolean) -> Unit,
    isMarkAsReadOnScroll: Boolean,
    onMarkAsReadOnScroll: (Boolean) -> Unit,
    maxLines: Int,
    setMaxLines: (Int) -> Unit,
    showOnlyTitle: Boolean,
    onShowOnlyTitle: (Boolean) -> Unit,
    isOpenAdjacent: Boolean,
    onOpenAdjacent: (Boolean) -> Unit,
    showReadingTime: Boolean,
    onShowReadingTimeChange: (Boolean) -> Unit,
    showTitleUnreadCount: Boolean,
    onShowTitleUnreadCountChange: (Boolean) -> Unit,
    onStartActivity: (intent: Intent) -> Unit,
    openAIState: OpenAISettingsState,
    onOpenAIEvent: (OpenAISettingsEvent) -> Unit,
    isOpenDrawerOnFab: Boolean,
    onOpenDrawerOnFab: (Boolean) -> Unit,
    currentFontSelection: FontSelection,
    onTextSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val dimens = LocalDimens.current
    val isAndroidSAndAbove =
        remember {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .padding(horizontal = dimens.margin)
                .fillMaxWidth()
                .verticalScroll(scrollState),
    ) {
        MenuSetting(
            title = stringResource(id = R.string.theme),
            currentValue = currentThemeValue,
            values =
                immutableListHolderOf(
                    ThemeOptions.SYSTEM.asThemeOption(),
                    ThemeOptions.DAY.asThemeOption(),
                    ThemeOptions.NIGHT.asThemeOption(),
                    ThemeOptions.E_INK.asThemeOption(),
                ),
            onSelection = onThemeChange,
            modifier =
                Modifier
                    .width(dimens.maxContentWidth),
        )

        SwitchSetting(
            title = stringResource(id = R.string.dynamic_theme_use),
            checked = useDynamicTheme,
            onCheckedChange = onUseDynamicTheme,
            description =
                when {
                    isAndroidSAndAbove -> {
                        null
                    }

                    else -> {
                        stringResource(
                            id = R.string.only_available_on_android_n,
                            "12",
                        )
                    }
                },
            enabled = isAndroidSAndAbove,
        )

        MenuSetting(
            title = stringResource(id = R.string.dark_theme_preference),
            currentValue = currentDarkThemePreference,
            values =
                immutableListHolderOf(
                    DarkThemePreferences.BLACK.asDarkThemeOption(),
                    DarkThemePreferences.DARK.asDarkThemeOption(),
                ),
            onSelection = onDarkThemePreferenceChange,
            modifier =
                Modifier
                    .width(dimens.maxContentWidth),
        )

        ListDialogSetting(
            title = stringResource(id = R.string.block_list),
            dialogTitle = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.block_list),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(id = R.string.block_list_description),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "feeder feed?r fe*er",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = LocalTypographySettings.current.monoFontFamily),
                    )
                }
            },
            currentValue = blockListValue,
            onAddItem = onBlockListAdd,
            onRemoveItem = onBlockListRemove,
        )

        NotificationsSetting(
            items = feedsSettings,
            onToggleItem = onToggleNotification,
        )

        HorizontalDivider(modifier = Modifier.width(dimens.maxContentWidth))

        SettingsGroup(
            title = R.string.text,
        ) {
            val systemDefaultString = stringResource(R.string.system_default)
            val currentUiFontOption =
                remember(currentFontSelection, systemDefaultString) {
                    UiFontOption.fromFontSelection(currentFontSelection, systemDefaultString)
                }
            ExternalSetting(
                currentUiFontOption.name,
                title = stringResource(R.string.text_settings),
                onClick = onTextSettings,
            )
        }

        HorizontalDivider(modifier = Modifier.width(dimens.maxContentWidth))

        SettingsGroup(
            title = R.string.synchronization,
        ) {
            MenuSetting(
                title = stringResource(id = R.string.check_for_updates),
                currentValue = currentSyncFrequencyValue.asSyncFreqOption(),
                values =
                    ImmutableHolder(
                        SyncFrequency.values().map {
                            it.asSyncFreqOption()
                        },
                    ),
                onSelection = {
                    onSyncFrequencyChange(it.syncFrequency)
                },
                modifier =
                    Modifier
                        .width(dimens.maxContentWidth),
            )

            SwitchSetting(
                title = stringResource(id = R.string.on_startup),
                checked = syncOnStartupValue,
                onCheckedChange = onSyncOnStartupChange,
            )

            SwitchSetting(
                title = stringResource(id = R.string.only_on_wifi),
                checked = syncOnlyOnWifiValue,
                onCheckedChange = onSyncOnlyOnWifiChange,
            )

            SwitchSetting(
                title = stringResource(id = R.string.only_when_charging),
                checked = syncOnlyWhenChargingValue,
                onCheckedChange = onSyncOnlyWhenChargingChange,
            )

            MenuSetting(
                title = stringResource(id = R.string.max_feed_items),
                currentValue = maxItemsPerFeedValue,
                values =
                    immutableListHolderOf(
                        50,
                        100,
                        200,
                        500,
                        1000,
                    ),
                onSelection = onMaxItemsPerFeedChange,
                modifier =
                    Modifier
                        .width(dimens.maxContentWidth),
            )

            ExternalSetting(
                currentValue =
                    when (batteryOptimizationIgnoredValue) {
                        true -> stringResource(id = R.string.battery_optimization_disabled)
                        false -> stringResource(id = R.string.battery_optimization_enabled)
                    },
                title = stringResource(id = R.string.battery_optimization),
                {
                    onStartActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                },
            )

            ExternalSetting(
                currentValue = "",
                title = stringResource(id = R.string.device_sync),
                {
                    onOpenSyncSettings()
                },
            )
        }

        HorizontalDivider(modifier = Modifier.width(dimens.maxContentWidth))

        SettingsGroup(
            title = R.string.article_list_settings,
        ) {
            MenuSetting(
                title = stringResource(id = R.string.sort),
                currentValue = currentSortingValue,
                values =
                    immutableListHolderOf(
                        SortingOptions.NEWEST_FIRST.asSortOption(),
                        SortingOptions.OLDEST_FIRST.asSortOption(),
                    ),
                onSelection = onSortingChange,
                modifier =
                    Modifier
                        .width(dimens.maxContentWidth),
            )

            SwitchSetting(
                title = stringResource(id = R.string.show_fab),
                checked = showFabValue,
                onCheckedChange = onShowFabChange,
            )

            SwitchSetting(
                title = stringResource(id = R.string.open_drawer_on_fab),
                checked = isOpenDrawerOnFab,
                onCheckedChange = onOpenDrawerOnFab,
            )

            MenuSetting(
                title = stringResource(id = R.string.feed_item_style),
                currentValue = feedItemStyleValue.asFeedItemStyleOption(),
                values = ImmutableHolder(FeedItemStyle.values().map { it.asFeedItemStyleOption() }),
                onSelection = {
                    onFeedItemStyleChange(it.feedItemStyle)
                },
                modifier =
                    Modifier
                        .width(dimens.maxContentWidth),
            )

            MenuSetting(
                title = stringResource(id = R.string.max_lines),
                currentValue = maxLines,
                values = ImmutableHolder(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                onSelection = setMaxLines,
                modifier =
                    Modifier
                        .width(dimens.maxContentWidth),
            )

            SwitchSetting(
                title = stringResource(id = R.string.show_only_title),
                checked = showOnlyTitle,
                onCheckedChange = onShowOnlyTitle,
            )

            MenuSetting(
                title = stringResource(id = R.string.swipe_to_mark_as_read),
                currentValue = swipeAsReadValue.asSwipeAsReadOption(),
                values = ImmutableHolder(SwipeAsRead.values().map { it.asSwipeAsReadOption() }),
                onSelection = {
                    onSwipeAsReadOptionChange(it.swipeAsRead)
                },
                modifier =
                    Modifier
                        .width(dimens.maxContentWidth),
            )

            SwitchSetting(
                title = stringResource(id = R.string.mark_as_read_on_scroll),
                checked = isMarkAsReadOnScroll,
                onCheckedChange = onMarkAsReadOnScroll,
            )

            SwitchSetting(
                title = stringResource(id = R.string.show_thumbnails),
                checked = showThumbnailsValue,
                onCheckedChange = onShowThumbnailsChange,
            )

            SwitchSetting(
                title = stringResource(id = R.string.show_reading_time),
                checked = showReadingTime,
                onCheckedChange = onShowReadingTimeChange,
            )

            SwitchSetting(
                title = stringResource(id = R.string.show_title_unread_count),
                checked = showTitleUnreadCount,
                onCheckedChange = onShowTitleUnreadCountChange,
            )
        }

        HorizontalDivider(modifier = Modifier.width(dimens.maxContentWidth))

        SettingsGroup(
            title = R.string.reader_settings,
        ) {
            MenuSetting(
                title = stringResource(id = R.string.open_item_by_default_with),
                currentValue = currentItemOpenerValue.asItemOpenerOption(),
                values =
                    immutableListHolderOf(
                        ItemOpener.READER.asItemOpenerOption(),
                        ItemOpener.CUSTOM_TAB.asItemOpenerOption(),
                        ItemOpener.DEFAULT_BROWSER.asItemOpenerOption(),
                    ),
                onSelection = {
                    onItemOpenerChange(it.itemOpener)
                },
                modifier =
                    Modifier
                        .width(dimens.maxContentWidth),
            )

            MenuSetting(
                title = stringResource(id = R.string.open_links_with),
                currentValue = currentLinkOpenerValue.asLinkOpenerOption(),
                values =
                    immutableListHolderOf(
                        LinkOpener.CUSTOM_TAB.asLinkOpenerOption(),
                        LinkOpener.DEFAULT_BROWSER.asLinkOpenerOption(),
                    ),
                onSelection = {
                    onLinkOpenerChange(it.linkOpener)
                },
                modifier =
                    Modifier
                        .width(dimens.maxContentWidth),
            )

            val notCompactScreen = LocalConfiguration.current.smallestScreenWidthDp >= 600

            if (notCompactScreen) {
                SwitchSetting(
                    title = stringResource(id = R.string.open_browser_in_split_screen),
                    checked = isOpenAdjacent,
                    onCheckedChange = onOpenAdjacent,
                )
            }
        }

        HorizontalDivider(modifier = Modifier.width(dimens.maxContentWidth))

        SettingsGroup(
            title = R.string.image_loading,
        ) {
            SwitchSetting(
                title = stringResource(id = R.string.only_on_wifi),
                checked = loadImageOnlyOnWifiValue,
                onCheckedChange = onLoadImageOnlyOnWifiChange,
            )
        }

        HorizontalDivider(modifier = Modifier.width(dimens.maxContentWidth))

        SettingsGroup(
            title = R.string.text_to_speech,
        ) {
            SwitchSetting(
                title = stringResource(id = R.string.use_detect_language),
                checked = useDetectLanguage,
                onCheckedChange = onUseDetectLanguageChange,
                description = stringResource(id = R.string.description_for_read_aloud),
            )
        }

        HorizontalDivider(modifier = Modifier.width(dimens.maxContentWidth))

        SettingsGroup(
            title = R.string.openai_settings,
        ) {
            OpenAISection(
                state = openAIState,
                onEvent = onOpenAIEvent,
            )
        }

        HorizontalDivider(modifier = Modifier.width(dimens.maxContentWidth))

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
fun ColumnScope.SettingsGroup(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    GroupTitle(
        modifier = modifier,
    ) { innerModifier ->
        Text(
            stringResource(id = title),
            modifier = innerModifier,
        )
    }

    content()
}

@Composable
fun GroupTitle(
    modifier: Modifier = Modifier,
    startingSpace: Boolean = true,
    height: Dp = 64.dp,
    title: @Composable (Modifier) -> Unit,
) {
    val dimens = LocalDimens.current
    Row(
        modifier =
            modifier
                .width(dimens.maxContentWidth),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (startingSpace) {
            Box(
                modifier =
                    Modifier
                        .width(64.dp)
                        .height(height),
            )
        }
        Box(
            modifier =
                Modifier
                    .height(height),
            contentAlignment = Alignment.CenterStart,
        ) {
            ProvideTextStyle(
                value =
                    MaterialTheme.typography.labelMedium.merge(
                        TextStyle(color = MaterialTheme.colorScheme.primary),
                    ),
            ) {
                title(Modifier.semantics { heading() })
            }
        }
    }
}

@Composable
fun ExternalSetting(
    currentValue: String,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
) {
    val dimens = LocalDimens.current
    Row(
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .clickable { onClick() }
                .semantics {
                    role = Role.Button
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                icon()
            }
        }

        TitleAndSubtitle(
            title = {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            subtitle = {
                Text(
                    currentValue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
    }
}

@Composable
fun <T> MenuSetting(
    title: String,
    currentValue: T,
    values: ImmutableHolder<List<T>>,
    onSelection: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = {},
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val dimens = LocalDimens.current
    val closeMenuText = stringResource(id = R.string.close_menu)
    Row(
        modifier =
            modifier
                .clickable { expanded = !expanded }
                .semantics {
                    role = Role.Button
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    icon()
                }
            }
        }

        TitleAndSubtitle(
            title = {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            subtitle = {
                Text(
                    currentValue.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier =
                Modifier.onKeyEventLikeEscape {
                    expanded = false
                },
        ) {
            // Hidden button for TalkBack
            DropdownMenuItem(
                onClick = {
                    expanded = false
                },
                text = {},
                modifier =
                    Modifier
                        .height(0.dp)
                        .safeSemantics {
                            contentDescription = closeMenuText
                            role = Role.Button
                        },
            )
            for (value in values.item) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelection(value)
                    },
                    text = {
                        val style =
                            if (value == currentValue) {
                                MaterialTheme.typography.bodyLarge.merge(
                                    TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                    ),
                                )
                            } else {
                                MaterialTheme.typography.bodyLarge
                            }
                        Text(
                            value.toString(),
                            style = style,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun ListDialogSetting(
    title: String,
    dialogTitle: @Composable () -> Unit,
    currentValue: ImmutableHolder<List<String>>,
    onAddItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val dimens = LocalDimens.current
    Row(
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .clickable { expanded = !expanded }
                .semantics {
                    role = Role.Button
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                icon()
            }
        }

        TitleAndSubtitle(
            title = {
                Text(title)
            },
            subtitle = {
                Text(
                    text = currentValue.item.joinToString(" ", limit = 5),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            },
        )

        if (expanded) {
            EditableListDialog(
                title = dialogTitle,
                items = currentValue,
                onDismiss = {
                    expanded = false
                },
                onAddItem = onAddItem,
                onRemoveItem = onRemoveItem,
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationsSetting(
    items: ImmutableHolder<List<UIFeedSettings>>,
    onToggleItem: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val notificationsPermissionState =
        rememberApiPermissionState(
            permission = "android.permission.POST_NOTIFICATIONS",
            minimumApiLevel = 33,
        ) { value ->
            expanded = value
        }

    val shouldShowExplanationForPermission by remember {
        derivedStateOf {
            notificationsPermissionState.status.shouldShowRationale
        }
    }

    val permissionDenied by remember {
        derivedStateOf {
            notificationsPermissionState.status is PermissionStatus.Denied
        }
    }

    var permissionDismissed by rememberSaveable {
        mutableStateOf(true)
    }

    val dimens = LocalDimens.current
    Row(
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .clickable {
                    when (notificationsPermissionState.status) {
                        is PermissionStatus.Denied -> {
                            if (notificationsPermissionState.status.shouldShowRationale) {
                                permissionDismissed = false
                            } else {
                                notificationsPermissionState.launchPermissionRequest()
                            }
                        }

                        PermissionStatus.Granted -> expanded = true
                    }
                }.semantics {
                    role = Role.Button
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                icon()
            }
        }

        TitleAndSubtitle(
            title = {
                Text(stringResource(id = R.string.notify_for_new_items))
            },
            subtitle = {
                Text(
                    text =
                        when (permissionDenied) {
                            true -> stringResource(id = R.string.explanation_permission_notifications)
                            false -> {
                                items.item
                                    .asSequence()
                                    .filter { it.notify }
                                    .map { it.title }
                                    .take(4)
                                    .joinToString(", ", limit = 3)
                            }
                        },
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            },
        )

        if (shouldShowExplanationForPermission && !permissionDismissed) {
            ExplainPermissionDialog(
                explanation = R.string.explanation_permission_notifications,
                onDismiss = {
                    permissionDismissed = true
                },
            ) {
                notificationsPermissionState.launchPermissionRequest()
            }
        } else if (expanded && permissionDismissed) {
            FeedNotificationsDialog(
                title = {
                    Text(
                        text = stringResource(id = R.string.notify_for_new_items),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                onToggleItem = onToggleItem,
                items = items,
                onDismiss = {
                    expanded = false
                },
            )
        }
    }
}

@Composable
fun RadioButtonSetting(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = {},
    minHeight: Dp = 64.dp,
) {
    val stateLabel =
        if (selected) {
            stringResource(androidx.compose.ui.R.string.selected)
        } else {
            stringResource(androidx.compose.ui.R.string.not_selected)
        }
    val dimens = LocalDimens.current
    Row(
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .heightIn(min = minHeight)
                .clickable { onClick() }
                .safeSemantics(mergeDescendants = true) {
                    role = Role.RadioButton
                    stateDescription = stateLabel
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    icon()
                }
            }
        }

        TitleAndSubtitle(
            title = {
                Text(title)
            },
        )

        Spacer(modifier = Modifier.width(8.dp))

        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

@Composable
fun SwitchSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: @Composable (() -> Unit)? = {},
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val dimens = LocalDimens.current
    Row(
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .heightIn(min = 64.dp)
                .clickable(
                    enabled = enabled,
                    onClick = { onCheckedChange(!checked) },
                ).safeSemantics(mergeDescendants = true) {
                    stateDescription =
                        when (checked) {
                            true -> context.getString(R.string.on)
                            else -> context.getString(R.string.off)
                        }
                    role = Role.Switch
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    icon()
                }
            }
        }

        TitleAndSubtitle(
            title = {
                Text(title)
            },
            subtitle =
                description?.let {
                    { Text(it) }
                },
        )

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.clearAndSetSemantics { },
            enabled = enabled,
        )
    }
}

@Composable
fun RowScope.TitleAndSubtitle(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.weight(1f),
        verticalArrangement = Arrangement.Center,
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.titleMedium) {
            title()
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.size(2.dp))
            ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                subtitle()
            }
        }
    }
}

@Composable
fun ThemeOptions.asThemeOption() =
    ThemeOption(
        currentTheme = this,
        name = stringResource(id = stringId),
    )

@Immutable
data class ThemeOption(
    val currentTheme: ThemeOptions,
    val name: String,
) {
    override fun toString() = name
}

@Composable
fun DarkThemePreferences.asDarkThemeOption() =
    DarkThemeOption(
        darkThemePreferences = this,
        name = stringResource(id = stringId),
    )

@Immutable
data class DarkThemeOption(
    val darkThemePreferences: DarkThemePreferences,
    val name: String,
) {
    override fun toString() = name
}

@Composable
fun SortingOptions.asSortOption() =
    SortOption(
        currentSorting = this,
        name = stringResource(id = stringId),
    )

@Immutable
data class SortOption(
    val currentSorting: SortingOptions,
    val name: String,
) {
    override fun toString() = name
}

@Immutable
data class FeedItemStyleOption(
    val feedItemStyle: FeedItemStyle,
    val name: String,
) {
    override fun toString() = name
}

@Immutable
data class SwipeAsReadOption(
    val swipeAsRead: SwipeAsRead,
    val name: String,
) {
    override fun toString() = name
}

@Immutable
data class SyncFreqOption(
    val syncFrequency: SyncFrequency,
    val name: String,
) {
    override fun toString() = name
}

@Composable
fun SyncFrequency.asSyncFreqOption() =
    SyncFreqOption(
        syncFrequency = this,
        name = stringResource(id = stringId),
    )

@Immutable
data class ItemOpenerOption(
    val itemOpener: ItemOpener,
    val name: String,
) {
    override fun toString() = name
}

@Composable
fun ItemOpener.asItemOpenerOption() =
    ItemOpenerOption(
        itemOpener = this,
        name = stringResource(id = stringId),
    )

@Immutable
data class LinkOpenerOption(
    val linkOpener: LinkOpener,
    val name: String,
) {
    override fun toString() = name
}

@Composable
fun LinkOpener.asLinkOpenerOption() =
    LinkOpenerOption(
        linkOpener = this,
        name = stringResource(id = stringId),
    )

@Composable
fun FeedItemStyle.asFeedItemStyleOption() =
    FeedItemStyleOption(
        feedItemStyle = this,
        name = stringResource(id = stringId),
    )

@Composable
fun SwipeAsRead.asSwipeAsReadOption() =
    SwipeAsReadOption(
        swipeAsRead = this,
        name = stringResource(id = stringId),
    )
