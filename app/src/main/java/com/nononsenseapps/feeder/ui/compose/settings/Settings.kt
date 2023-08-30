package com.nononsenseapps.feeder.ui.compose.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.SensibleTopAppBar
import com.nononsenseapps.feeder.ui.compose.theme.SetStatusBarColorToMatchScrollableTopAppBar
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.immutableListHolderOf
import com.nononsenseapps.feeder.ui.compose.utils.isCompactDevice
import com.nononsenseapps.feeder.ui.compose.utils.onKeyEventLikeEscape
import com.nononsenseapps.feeder.ui.compose.utils.rememberApiPermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToSyncScreen: () -> Unit,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val viewState by settingsViewModel.viewState.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    SetStatusBarColorToMatchScrollableTopAppBar(scrollBehavior)

    Scaffold(
        modifier = modifier
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
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        SettingsList(
            currentThemeValue = viewState.currentTheme.asThemeOption(),
            onThemeChanged = { value ->
                settingsViewModel.setCurrentTheme(value.currentTheme)
            },
            currentDarkThemePreference = viewState.darkThemePreference.asDarkThemeOption(),
            onDarkThemePreferenceChanged = { value ->
                settingsViewModel.setPreferredDarkTheme(value.darkThemePreferences)
            },
            currentSortingValue = viewState.currentSorting.asSortOption(),
            onSortingChanged = { value ->
                settingsViewModel.setCurrentSorting(value.currentSorting)
            },
            showFabValue = viewState.showFab,
            onShowFabChanged = settingsViewModel::setShowFab,
            feedItemStyleValue = viewState.feedItemStyle,
            onFeedItemStyleChanged = settingsViewModel::setFeedItemStyle,
            blockListValue = ImmutableHolder(viewState.blockList.sorted()),
            swipeAsReadValue = viewState.swipeAsRead,
            onSwipeAsReadOptionChanged = settingsViewModel::setSwipeAsRead,
            syncOnStartupValue = viewState.syncOnResume,
            onSyncOnStartupChanged = settingsViewModel::setSyncOnResume,
            syncOnlyOnWifiValue = viewState.syncOnlyOnWifi,
            onSyncOnlyOnWifiChanged = settingsViewModel::setSyncOnlyOnWifi,
            syncOnlyWhenChargingValue = viewState.syncOnlyWhenCharging,
            onSyncOnlyWhenChargingChanged = settingsViewModel::setSyncOnlyWhenCharging,
            loadImageOnlyOnWifiValue = viewState.loadImageOnlyOnWifi,
            onLoadImageOnlyOnWifiChanged = settingsViewModel::setLoadImageOnlyOnWifi,
            showThumbnailsValue = viewState.showThumbnails,
            onShowThumbnailsChanged = settingsViewModel::setShowThumbnails,
            useDetectLanguage = viewState.useDetectLanguage,
            onUseDetectLanguageChanged = settingsViewModel::setUseDetectLanguage,
            maxItemsPerFeedValue = viewState.maximumCountPerFeed,
            onMaxItemsPerFeedChanged = settingsViewModel::setMaxCountPerFeed,
            currentItemOpenerValue = viewState.itemOpener,
            onItemOpenerChanged = settingsViewModel::setItemOpener,
            currentLinkOpenerValue = viewState.linkOpener,
            onLinkOpenerChanged = settingsViewModel::setLinkOpener,
            currentSyncFrequencyValue = viewState.syncFrequency,
            onSyncFrequencyChanged = settingsViewModel::setSyncFrequency,
            batteryOptimizationIgnoredValue = viewState.batteryOptimizationIgnored,
            onOpenSyncSettings = onNavigateToSyncScreen,
            useDynamicTheme = viewState.useDynamicTheme,
            onUseDynamicTheme = settingsViewModel::setUseDynamicTheme,
            textScale = viewState.textScale,
            setTextScale = settingsViewModel::setTextScale,
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
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
@Preview(showBackground = true, device = PIXEL_C)
@Preview(showBackground = true, device = NEXUS_5)
fun SettingsScreenPreview() {
    FeederTheme(ThemeOptions.DAY) {
        Surface {
            SettingsList(
                currentThemeValue = ThemeOptions.SYSTEM.asThemeOption(),
                onThemeChanged = {},
                currentDarkThemePreference = DarkThemePreferences.BLACK.asDarkThemeOption(),
                onDarkThemePreferenceChanged = {},
                currentSortingValue = SortingOptions.NEWEST_FIRST.asSortOption(),
                onSortingChanged = {},
                showFabValue = true,
                onShowFabChanged = {},
                feedItemStyleValue = FeedItemStyle.CARD,
                onFeedItemStyleChanged = {},
                blockListValue = ImmutableHolder(emptyList()),
                swipeAsReadValue = SwipeAsRead.ONLY_FROM_END,
                onSwipeAsReadOptionChanged = {},
                syncOnStartupValue = true,
                onSyncOnStartupChanged = {},
                syncOnlyOnWifiValue = true,
                onSyncOnlyOnWifiChanged = {},
                syncOnlyWhenChargingValue = true,
                onSyncOnlyWhenChargingChanged = {},
                loadImageOnlyOnWifiValue = true,
                onLoadImageOnlyOnWifiChanged = {},
                showThumbnailsValue = true,
                onShowThumbnailsChanged = {},
                useDetectLanguage = true,
                onUseDetectLanguageChanged = {},
                maxItemsPerFeedValue = 101,
                onMaxItemsPerFeedChanged = {},
                currentItemOpenerValue = ItemOpener.CUSTOM_TAB,
                onItemOpenerChanged = {},
                currentLinkOpenerValue = LinkOpener.DEFAULT_BROWSER,
                onLinkOpenerChanged = {},
                currentSyncFrequencyValue = SyncFrequency.EVERY_12_HOURS,
                onSyncFrequencyChanged = {},
                batteryOptimizationIgnoredValue = false,
                onOpenSyncSettings = {},
                useDynamicTheme = true,
                onUseDynamicTheme = {},
                textScale = 1.5f,
                setTextScale = {},
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
                modifier = Modifier,
            )
        }
    }
}

@Composable
fun SettingsList(
    currentThemeValue: ThemeOption,
    onThemeChanged: (ThemeOption) -> Unit,
    currentDarkThemePreference: DarkThemeOption,
    onDarkThemePreferenceChanged: (DarkThemeOption) -> Unit,
    currentSortingValue: SortOption,
    onSortingChanged: (SortOption) -> Unit,
    showFabValue: Boolean,
    onShowFabChanged: (Boolean) -> Unit,
    feedItemStyleValue: FeedItemStyle,
    onFeedItemStyleChanged: (FeedItemStyle) -> Unit,
    blockListValue: ImmutableHolder<List<String>>,
    swipeAsReadValue: SwipeAsRead,
    onSwipeAsReadOptionChanged: (SwipeAsRead) -> Unit,
    syncOnStartupValue: Boolean,
    onSyncOnStartupChanged: (Boolean) -> Unit,
    syncOnlyOnWifiValue: Boolean,
    onSyncOnlyOnWifiChanged: (Boolean) -> Unit,
    syncOnlyWhenChargingValue: Boolean,
    onSyncOnlyWhenChargingChanged: (Boolean) -> Unit,
    loadImageOnlyOnWifiValue: Boolean,
    onLoadImageOnlyOnWifiChanged: (Boolean) -> Unit,
    showThumbnailsValue: Boolean,
    onShowThumbnailsChanged: (Boolean) -> Unit,
    useDetectLanguage: Boolean,
    onUseDetectLanguageChanged: (Boolean) -> Unit,
    maxItemsPerFeedValue: Int,
    onMaxItemsPerFeedChanged: (Int) -> Unit,
    currentItemOpenerValue: ItemOpener,
    onItemOpenerChanged: (ItemOpener) -> Unit,
    currentLinkOpenerValue: LinkOpener,
    onLinkOpenerChanged: (LinkOpener) -> Unit,
    currentSyncFrequencyValue: SyncFrequency,
    onSyncFrequencyChanged: (SyncFrequency) -> Unit,
    batteryOptimizationIgnoredValue: Boolean,
    onOpenSyncSettings: () -> Unit,
    useDynamicTheme: Boolean,
    onUseDynamicTheme: (Boolean) -> Unit,
    textScale: Float,
    setTextScale: (Float) -> Unit,
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
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val dimens = LocalDimens.current
    val isAndroidQAndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val isAndroidSAndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = dimens.margin)
            .fillMaxWidth()
            .verticalScroll(scrollState),
    ) {
        MenuSetting(
            currentValue = currentThemeValue,
            values = immutableListHolderOf(
                ThemeOptions.SYSTEM.asThemeOption(),
                ThemeOptions.DAY.asThemeOption(),
                ThemeOptions.NIGHT.asThemeOption(),
                ThemeOptions.E_INK.asThemeOption(),
            ),
            title = stringResource(id = R.string.theme),
            onSelection = onThemeChanged,
        )

        SwitchSetting(
            title = stringResource(id = R.string.dynamic_theme_use),
            checked = useDynamicTheme,
            description = when {
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
            onCheckedChanged = onUseDynamicTheme,
        )

        MenuSetting(
            title = stringResource(id = R.string.dark_theme_preference),
            currentValue = currentDarkThemePreference,
            values = immutableListHolderOf(
                DarkThemePreferences.BLACK.asDarkThemeOption(),
                DarkThemePreferences.DARK.asDarkThemeOption(),
            ),
            onSelection = onDarkThemePreferenceChanged,
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
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
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

        Divider(modifier = Modifier.width(dimens.maxContentWidth))

        GroupTitle { innerModifier ->
            Text(
                stringResource(id = R.string.text_scale),
                modifier = innerModifier,
            )
        }

        ScaleSetting(
            currentValue = textScale,
            onValueChange = setTextScale,
            valueRange = 1f..2f,
            steps = 9,
        )

        Divider(modifier = Modifier.width(dimens.maxContentWidth))

        GroupTitle { innerModifier ->
            Text(
                stringResource(id = R.string.synchronization),
                modifier = innerModifier,
            )
        }

        MenuSetting(
            currentValue = currentSyncFrequencyValue.asSyncFreqOption(),
            values = ImmutableHolder(
                SyncFrequency.values().map {
                    it.asSyncFreqOption()
                },
            ),
            title = stringResource(id = R.string.check_for_updates),
            onSelection = {
                onSyncFrequencyChanged(it.syncFrequency)
            },
        )

        SwitchSetting(
            title = stringResource(id = R.string.on_startup),
            checked = syncOnStartupValue,
            onCheckedChanged = onSyncOnStartupChanged,
        )

        SwitchSetting(
            title = stringResource(id = R.string.only_on_wifi),
            checked = syncOnlyOnWifiValue,
            onCheckedChanged = onSyncOnlyOnWifiChanged,
        )

        SwitchSetting(
            title = stringResource(id = R.string.only_when_charging),
            checked = syncOnlyWhenChargingValue,
            onCheckedChanged = onSyncOnlyWhenChargingChanged,
        )

        MenuSetting(
            currentValue = maxItemsPerFeedValue,
            values = immutableListHolderOf(
                50,
                100,
                200,
                500,
                1000,
            ),
            title = stringResource(id = R.string.max_feed_items),
            onSelection = onMaxItemsPerFeedChanged,
        )

        ExternalSetting(
            currentValue = when (batteryOptimizationIgnoredValue) {
                true -> stringResource(id = R.string.battery_optimization_disabled)
                false -> stringResource(id = R.string.battery_optimization_enabled)
            },
            title = stringResource(id = R.string.battery_optimization),
        ) {
            context.startActivity(
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            )
        }

        ExternalSetting(
            currentValue = "",
            title = stringResource(id = R.string.device_sync),
        ) {
            onOpenSyncSettings()
        }

        Divider(modifier = Modifier.width(dimens.maxContentWidth))

        GroupTitle { innerModifier ->
            Text(
                stringResource(id = R.string.article_list_settings),
                modifier = innerModifier,
            )
        }

        MenuSetting(
            currentValue = currentSortingValue,
            values = immutableListHolderOf(
                SortingOptions.NEWEST_FIRST.asSortOption(),
                SortingOptions.OLDEST_FIRST.asSortOption(),
            ),
            title = stringResource(id = R.string.sort),
            onSelection = onSortingChanged,
        )

        SwitchSetting(
            title = stringResource(id = R.string.show_fab),
            checked = showFabValue,
            onCheckedChanged = onShowFabChanged,
        )

        if (isCompactDevice()) {
            MenuSetting(
                title = stringResource(id = R.string.feed_item_style),
                currentValue = feedItemStyleValue.asFeedItemStyleOption(),
                values = ImmutableHolder(FeedItemStyle.values().map { it.asFeedItemStyleOption() }),
                onSelection = {
                    onFeedItemStyleChanged(it.feedItemStyle)
                },
            )
        }

        MenuSetting(
            title = stringResource(id = R.string.max_lines),
            currentValue = maxLines,
            values = ImmutableHolder(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
            onSelection = setMaxLines,
        )

        SwitchSetting(
            title = stringResource(id = R.string.show_only_title),
            checked = showOnlyTitle,
            onCheckedChanged = onShowOnlyTitle,
        )

        MenuSetting(
            title = stringResource(id = R.string.swipe_to_mark_as_read),
            currentValue = swipeAsReadValue.asSwipeAsReadOption(),
            values = ImmutableHolder(SwipeAsRead.values().map { it.asSwipeAsReadOption() }),
            onSelection = {
                onSwipeAsReadOptionChanged(it.swipeAsRead)
            },
        )

        SwitchSetting(
            title = stringResource(id = R.string.mark_as_read_on_scroll),
            checked = isMarkAsReadOnScroll,
            onCheckedChanged = onMarkAsReadOnScroll,
        )

        SwitchSetting(
            title = stringResource(id = R.string.show_thumbnails),
            checked = showThumbnailsValue,
            onCheckedChanged = onShowThumbnailsChanged,
        )

        Divider(modifier = Modifier.width(dimens.maxContentWidth))

        GroupTitle { innerModifier ->
            Text(
                stringResource(id = R.string.reader_settings),
                modifier = innerModifier,
            )
        }

        MenuSetting(
            currentValue = currentItemOpenerValue.asItemOpenerOption(),
            values = immutableListHolderOf(
                ItemOpener.READER.asItemOpenerOption(),
                ItemOpener.CUSTOM_TAB.asItemOpenerOption(),
                ItemOpener.DEFAULT_BROWSER.asItemOpenerOption(),
            ),
            title = stringResource(id = R.string.open_item_by_default_with),
            onSelection = {
                onItemOpenerChanged(it.itemOpener)
            },
        )

        MenuSetting(
            currentValue = currentLinkOpenerValue.asLinkOpenerOption(),
            values = immutableListHolderOf(
                LinkOpener.CUSTOM_TAB.asLinkOpenerOption(),
                LinkOpener.DEFAULT_BROWSER.asLinkOpenerOption(),
            ),
            title = stringResource(id = R.string.open_links_with),
            onSelection = {
                onLinkOpenerChanged(it.linkOpener)
            },
        )

        Divider(modifier = Modifier.width(dimens.maxContentWidth))

        GroupTitle { innerModifier ->
            Text(
                stringResource(id = R.string.image_loading),
                modifier = innerModifier,
            )
        }

        SwitchSetting(
            title = stringResource(id = R.string.only_on_wifi),
            checked = loadImageOnlyOnWifiValue,
            onCheckedChanged = onLoadImageOnlyOnWifiChanged,
        )

        Divider(modifier = Modifier.width(dimens.maxContentWidth))

        GroupTitle { innerModifier ->
            Text(
                stringResource(id = R.string.text_to_speech),
                modifier = innerModifier,
            )
        }

        SwitchSetting(
            title = stringResource(id = R.string.use_detect_language),
            checked = useDetectLanguage,
            description = when {
                isAndroidQAndAbove -> stringResource(id = R.string.description_for_read_aloud)
                else -> stringResource(
                    id = R.string.only_available_on_android_n,
                    "10",
                )
            },
            enabled = isAndroidQAndAbove,
            onCheckedChanged = onUseDetectLanguageChanged,
        )

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
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
        modifier = modifier
            .width(dimens.maxContentWidth),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (startingSpace) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(height),
            )
        }
        Box(
            modifier = Modifier
                .height(height),
            contentAlignment = Alignment.CenterStart,
        ) {
            ProvideTextStyle(
                value = MaterialTheme.typography.labelMedium.merge(
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
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    val dimens = LocalDimens.current
    Row(
        modifier = modifier
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
                Text(title)
            },
            subtitle = {
                Text(currentValue)
            },
        )
    }
}

@Composable
fun <T> MenuSetting(
    title: String,
    currentValue: T,
    values: ImmutableHolder<List<T>>,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    onSelection: (T) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val dimens = LocalDimens.current
    Row(
        modifier = modifier
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
                Text(currentValue.toString())
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.onKeyEventLikeEscape {
                expanded = false
            },
        ) {
            for (value in values.item) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelection(value)
                    },
                    text = {
                        val style = if (value == currentValue) {
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
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
    onRemoveItem: (String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val dimens = LocalDimens.current
    Row(
        modifier = modifier
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

    val notificationsPermissionState = rememberApiPermissionState(
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
        modifier = modifier
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
            }
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
                Text(stringResource(id = R.string.notify_for_new_items))
            },
            subtitle = {
                Text(
                    text = when (permissionDenied) {
                        true -> stringResource(id = R.string.explanation_permission_notifications)
                        false -> {
                            items.item.asSequence()
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
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = {},
    minHeight: Dp = 64.dp,
    onClick: () -> Unit,
) {
    val stateLabel = if (selected) {
        stringResource(androidx.compose.ui.R.string.selected)
    } else {
        stringResource(androidx.compose.ui.R.string.not_selected)
    }
    val dimens = LocalDimens.current
    Row(
        modifier = modifier
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
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: @Composable (() -> Unit)? = {},
    enabled: Boolean = true,
    onCheckedChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val dimens = LocalDimens.current
    Row(
        modifier = modifier
            .width(dimens.maxContentWidth)
            .heightIn(min = 64.dp)
            .clickable(
                enabled = enabled,
                onClick = { onCheckedChanged(!checked) },
            )
            .safeSemantics(mergeDescendants = true) {
                stateDescription = when (checked) {
                    true -> context.getString(androidx.compose.ui.R.string.on)
                    else -> context.getString(androidx.compose.ui.R.string.off)
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
            subtitle = description?.let {
                { Text(it) }
            },
        )

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChanged,
            modifier = Modifier.clearAndSetSemantics { },
            enabled = enabled,
        )
    }
}

@Composable
fun ScaleSetting(
    currentValue: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier,
) {
    val dimens = LocalDimens.current
    val safeCurrentValue = currentValue.coerceIn(valueRange)
    // People using screen readers probably don't care that much about text size
    // so no point in adding screen reader action?
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .width(dimens.maxContentWidth)
            .heightIn(min = 64.dp)
            .padding(start = 64.dp)
            .safeSemantics(mergeDescendants = true) {
                stateDescription = "%.1fx".format(safeCurrentValue)
            },
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            tonalElevation = 3.dp,
        ) {
            Text(
                "Lorem ipsum dolor sit amet.",
                style = MaterialTheme.typography.bodyLarge
                    .merge(
                        TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * currentValue,
                        ),
                    ),
                modifier = Modifier.padding(4.dp),
            )
        }
        SliderWithEndLabels(
            value = safeCurrentValue,
            startLabel = {
                Text(
                    "A",
                    style = MaterialTheme.typography.bodyLarge
                        .merge(
                            TextStyle(
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize * valueRange.start,
                            ),
                        ),
                    modifier = Modifier.alignByBaseline(),
                )
            },
            endLabel = {
                Text(
                    "A",
                    style = MaterialTheme.typography.bodyLarge
                        .merge(
                            TextStyle(
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize * valueRange.endInclusive,
                            ),
                        ),
                    modifier = Modifier.alignByBaseline(),
                )
            },
            valueRange = valueRange,
            steps = steps,
            onValueChange = onValueChange,
        )
    }
}

@Composable
private fun RowScope.TitleAndSubtitle(
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier.weight(1f),
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
