package com.nononsenseapps.feeder.ui.compose.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.DarkThemePreferences
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.archmodel.ItemOpener
import com.nononsenseapps.feeder.archmodel.LinkOpener
import com.nononsenseapps.feeder.archmodel.SortingOptions
import com.nononsenseapps.feeder.archmodel.SwipeAsRead
import com.nononsenseapps.feeder.archmodel.SyncFrequency
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.ui.compose.dialog.EditableListDialog
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.immutableListHolderOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToSyncScreen: () -> Unit,
    settingsViewModel: SettingsViewModel,
) {
    val viewState by settingsViewModel.viewState.collectAsState()

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .systemBarsPadding(),
        topBar = {
            SmallTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = stringResource(id = R.string.title_activity_settings),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        SettingsList(
            modifier = Modifier.padding(padding),
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
            blockListValue = ImmutableHolder(viewState.blockList),
            onBlockListChanged = settingsViewModel::setBlockList,
            syncOnStartupValue = viewState.syncOnResume,
            onSyncOnStartupChanged = settingsViewModel::setSyncOnResume,
            syncOnlyOnWifiValue = viewState.syncOnlyOnWifi,
            onSyncOnlyOnWifiChanged = settingsViewModel::setSyncOnlyOnWifi,
            syncOnlyWhenChargingValue = viewState.syncOnlyWhenCharging,
            onSyncOnlyWhenChargingChanged = settingsViewModel::setSyncOnlyWhenCharging,
            loadImageOnlyOnWifiValue = viewState.loadImageOnlyOnWifi,
            onLoadImageOnlyOnWifiChanged = settingsViewModel::setLoadImageOnlyOnWifi,
            useDetectLanguage = viewState.useDetectLanguage,
            onUseDetectLanguageChanged = settingsViewModel::setUseDetectLanguage,
            showThumbnailsValue = viewState.showThumbnails,
            onShowThumbnailsChanged = settingsViewModel::setShowThumbnails,
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
            swipeAsReadValue = viewState.swipeAsRead,
            onSwipeAsReadOptionChanged = settingsViewModel::setSwipeAsRead,
            useDynamicTheme = viewState.useDynamicTheme,
            onUseDynamicTheme = settingsViewModel::setUseDynamicTheme
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SettingsScreenPreview() {
    FeederTheme(ThemeOptions.DAY) {
        Surface {
            SettingsList(
                modifier = Modifier,
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
                blockListValue = ImmutableHolder(emptySet()),
                onBlockListChanged = {},
                syncOnStartupValue = true,
                onSyncOnStartupChanged = {},
                syncOnlyOnWifiValue = true,
                onSyncOnlyOnWifiChanged = {},
                syncOnlyWhenChargingValue = true,
                onSyncOnlyWhenChargingChanged = {},
                loadImageOnlyOnWifiValue = true,
                onLoadImageOnlyOnWifiChanged = {},
                showThumbnailsValue = true,
                useDetectLanguage = true,
                onUseDetectLanguageChanged = {},
                onShowThumbnailsChanged = {},
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
                onSwipeAsReadOptionChanged = {},
                swipeAsReadValue = SwipeAsRead.ONLY_FROM_END,
                useDynamicTheme = true,
                onUseDynamicTheme = {},
            )
        }
    }
}

@Composable
fun SettingsList(
    modifier: Modifier,
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
    blockListValue: ImmutableHolder<Set<String>>,
    onBlockListChanged: (Iterable<String>) -> Unit,
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
            .verticalScroll(scrollState)
    ) {
        MenuSetting(
            currentValue = currentThemeValue,
            values = immutableListHolderOf(
                ThemeOptions.SYSTEM.asThemeOption(),
                ThemeOptions.DAY.asThemeOption(),
                ThemeOptions.NIGHT.asThemeOption()
            ),
            title = stringResource(id = R.string.theme),
            onSelection = onThemeChanged
        )

        SwitchSetting(
            checked = useDynamicTheme,
            onCheckedChanged = onUseDynamicTheme,
            title = stringResource(id = R.string.dynamic_theme_use),
            description = when {
                isAndroidSAndAbove -> {
                    stringResource(
                        id = R.string.only_available_on_android_n,
                        "12"
                    )
                }
                else -> {
                    ""
                }
            },
            enabled = isAndroidSAndAbove
        )

        MenuSetting(
            title = stringResource(id = R.string.dark_theme_preference),
            currentValue = currentDarkThemePreference,
            values = immutableListHolderOf(
                DarkThemePreferences.BLACK.asDarkThemeOption(),
                DarkThemePreferences.DARK.asDarkThemeOption()
            ),
            onSelection = onDarkThemePreferenceChanged
        )

        MenuSetting(
            currentValue = currentSortingValue,
            values = immutableListHolderOf(
                SortingOptions.NEWEST_FIRST.asSortOption(),
                SortingOptions.OLDEST_FIRST.asSortOption()
            ),
            title = stringResource(id = R.string.sort),
            onSelection = onSortingChanged
        )

        SwitchSetting(
            checked = showFabValue,
            onCheckedChanged = onShowFabChanged,
            title = stringResource(id = R.string.show_fab)
        )

        MenuSetting(
            title = stringResource(id = R.string.feed_item_style),
            currentValue = feedItemStyleValue.asFeedItemStyleOption(),
            values = ImmutableHolder(FeedItemStyle.values().map { it.asFeedItemStyleOption() }),
            onSelection = {
                onFeedItemStyleChanged(it.feedItemStyle)
            },
        )

        MenuSetting(
            title = stringResource(id = R.string.swipe_to_mark_as_read),
            currentValue = swipeAsReadValue.asSwipeAsReadOption(),
            values = ImmutableHolder(SwipeAsRead.values().map { it.asSwipeAsReadOption() }),
            onSelection = {
                onSwipeAsReadOptionChanged(it.swipeAsRead)
            }
        )

        Divider(modifier = Modifier.width(dimens.maxContentWidth))

        GroupTitle { modifier ->
            Text(
                stringResource(id = R.string.synchronization),
                modifier = modifier,
            )
        }

        ListDialogSetting(
            currentValue = ImmutableHolder(blockListValue.item.sorted()),
            onSelection = onBlockListChanged,
            title = stringResource(id = R.string.block_list),
        )

        MenuSetting(
            currentValue = currentSyncFrequencyValue.asSyncFreqOption(),
            values = ImmutableHolder(
                SyncFrequency.values().map {
                    it.asSyncFreqOption()
                }
            ),
            title = stringResource(id = R.string.check_for_updates),
            onSelection = {
                onSyncFrequencyChanged(it.syncFrequency)
            }
        )

        SwitchSetting(
            checked = syncOnStartupValue,
            onCheckedChanged = onSyncOnStartupChanged,
            title = stringResource(id = R.string.on_startup)
        )

        SwitchSetting(
            checked = syncOnlyOnWifiValue,
            onCheckedChanged = onSyncOnlyOnWifiChanged,
            title = stringResource(id = R.string.only_on_wifi)
        )

        SwitchSetting(
            checked = syncOnlyWhenChargingValue,
            onCheckedChanged = onSyncOnlyWhenChargingChanged,
            title = stringResource(id = R.string.only_when_charging)
        )

        MenuSetting(
            currentValue = maxItemsPerFeedValue,
            values = immutableListHolderOf(
                50,
                100,
                200,
                500,
                1000
            ),
            title = stringResource(id = R.string.max_feed_items),
            onSelection = onMaxItemsPerFeedChanged
        )

        ExternalSetting(
            currentValue = when (batteryOptimizationIgnoredValue) {
                true -> stringResource(id = R.string.battery_optimization_disabled)
                false -> stringResource(id = R.string.battery_optimization_enabled)
            },
            title = stringResource(id = R.string.battery_optimization)
        ) {
            context.startActivity(
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            )
        }

        ExternalSetting(
            currentValue = "",
            title = stringResource(id = R.string.device_sync)
        ) {
            onOpenSyncSettings()
        }

        Divider(modifier = Modifier.width(dimens.maxContentWidth))

        GroupTitle { modifier ->
            Text(
                stringResource(id = R.string.image_loading),
                modifier = modifier,
            )
        }

        SwitchSetting(
            checked = loadImageOnlyOnWifiValue,
            onCheckedChanged = onLoadImageOnlyOnWifiChanged,
            title = stringResource(id = R.string.only_on_wifi)
        )

        SwitchSetting(
            checked = showThumbnailsValue,
            onCheckedChanged = onShowThumbnailsChanged,
            title = stringResource(id = R.string.show_thumbnails)
        )

        Divider(modifier = Modifier.width(dimens.maxContentWidth))

        GroupTitle { modifier ->
            Text(
                stringResource(id = R.string.reader_settings),
                modifier = modifier,
            )
        }

        MenuSetting(
            currentValue = currentItemOpenerValue.asItemOpenerOption(),
            values = immutableListHolderOf(
                ItemOpener.READER.asItemOpenerOption(),
                ItemOpener.CUSTOM_TAB.asItemOpenerOption(),
                ItemOpener.DEFAULT_BROWSER.asItemOpenerOption()
            ),
            title = stringResource(id = R.string.open_item_by_default_with),
            onSelection = {
                onItemOpenerChanged(it.itemOpener)
            }
        )

        MenuSetting(
            currentValue = currentLinkOpenerValue.asLinkOpenerOption(),
            values = immutableListHolderOf(
                LinkOpener.CUSTOM_TAB.asLinkOpenerOption(),
                LinkOpener.DEFAULT_BROWSER.asLinkOpenerOption()
            ),
            title = stringResource(id = R.string.open_links_with),
            onSelection = {
                onLinkOpenerChanged(it.linkOpener)
            }
        )

        Divider(modifier = Modifier.width(dimens.maxContentWidth))

        GroupTitle { modifier ->
            Text(
                stringResource(id = R.string.read_article),
                modifier = modifier,
            )
        }

        SwitchSetting(
            checked = useDetectLanguage,
            onCheckedChanged = onUseDetectLanguageChanged,
            title = stringResource(id = R.string.use_detect_language),
            description = when {
                isAndroidQAndAbove -> stringResource(id = R.string.description_for_read_aloud)
                else -> stringResource(
                    id = R.string.only_available_on_android_n,
                    "10"
                )
            },
            enabled = isAndroidQAndAbove
        )

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
fun GroupTitle(
    startingSpace: Boolean = true,
    height: Dp = 64.dp,
    title: @Composable (Modifier) -> Unit,
) {
    val dimens = LocalDimens.current
    Row(
        modifier = Modifier
            .width(dimens.maxContentWidth),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (startingSpace) {
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(height)
            )
        }
        Box(
            modifier = Modifier
                .height(height),
            contentAlignment = Alignment.CenterStart
        ) {
            ProvideTextStyle(
                value = MaterialTheme.typography.labelMedium.merge(
                    TextStyle(color = MaterialTheme.colorScheme.primary)
                )
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
    icon: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    val dimens = LocalDimens.current
    Row(
        modifier = Modifier
            .width(dimens.maxContentWidth)
            .clickable { onClick() }
            .semantics {
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
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
            }
        )
    }
}

@Composable
fun <T> MenuSetting(
    title: String,
    currentValue: T,
    values: ImmutableHolder<List<T>>,
    icon: @Composable () -> Unit = {},
    onSelection: (T) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val dimens = LocalDimens.current
    Row(
        modifier = Modifier
            .width(dimens.maxContentWidth)
            .clickable { expanded = !expanded }
            .semantics {
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
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
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
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
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge
                        }
                        Text(
                            value.toString(),
                            style = style
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ListDialogSetting(
    title: String,
    currentValue: ImmutableHolder<List<String>>,
    icon: @Composable () -> Unit = {},
    onSelection: (Iterable<String>) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val dimens = LocalDimens.current
    Row(
        modifier = Modifier
            .width(dimens.maxContentWidth)
            .clickable { expanded = !expanded }
            .semantics {
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
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
                    text = currentValue.item.joinToString(" "),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        )

        if (expanded) {
            EditableListDialog(
                title = title,
                items = currentValue,
                onDismiss = {
                    expanded = false
                },
                onModifiedItems = onSelection,
            )
        }
    }
}

@Composable
fun RadioButtonSetting(
    title: String,
    selected: Boolean,
    icon: (@Composable () -> Unit)? = {},
    minHeight: Dp = 64.dp,
    onClick: () -> Unit,
) {
    val stateLabel = if (selected) {
        stringResource(R.string.selected)
    } else {
        stringResource(R.string.not_selected)
    }
    val dimens = LocalDimens.current
    Row(
        modifier = Modifier
            .width(dimens.maxContentWidth)
            .heightIn(min = minHeight)
            .clickable { onClick() }
            .semantics(mergeDescendants = true) {
                role = Role.RadioButton
                stateDescription = stateLabel
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    icon()
                }
            }
        }

        TitleAndSubtitle(
            title = {
                Text(title)
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
}

@Composable
fun SwitchSetting(
    title: String,
    description: String? = null,
    checked: Boolean,
    icon: (@Composable () -> Unit)? = {},
    onCheckedChanged: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val stateLabel = if (checked) {
        stringResource(R.string.on)
    } else {
        stringResource(R.string.off)
    }
    val dimens = LocalDimens.current
    Row(
        modifier = Modifier
            .width(dimens.maxContentWidth)
            .heightIn(min = 64.dp)
            .clickable(
                enabled = enabled,
                onClick = { onCheckedChanged(!checked) }
            )
            .semantics(mergeDescendants = true) {
                stateDescription = stateLabel
                role = Role.Switch
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
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
            }
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
private fun RowScope.TitleAndSubtitle(
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center
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
        name = stringResource(id = stringId)
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
        name = stringResource(id = stringId)
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
        name = stringResource(id = stringId)
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
        name = stringResource(id = stringId)
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
        name = stringResource(id = stringId)
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
        name = stringResource(id = stringId)
    )

@Composable
fun FeedItemStyle.asFeedItemStyleOption() =
    FeedItemStyleOption(
        feedItemStyle = this,
        name = stringResource(id = stringId)
    )

@Composable
fun SwipeAsRead.asSwipeAsReadOption() =
    SwipeAsReadOption(
        swipeAsRead = this,
        name = stringResource(id = stringId)
    )
