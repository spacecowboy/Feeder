package com.nononsenseapps.feeder.ui.compose.settings

import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.ApplicationState
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.contentHorizontalPadding
import com.nononsenseapps.feeder.ui.compose.theme.upButtonStartPadding
import com.nononsenseapps.feeder.util.ItemOpener
import com.nononsenseapps.feeder.util.LinkOpener
import com.nononsenseapps.feeder.util.SortingOptions
import com.nononsenseapps.feeder.util.SyncFrequency
import com.nononsenseapps.feeder.util.ThemeOptions
import kotlinx.coroutines.flow.mapLatest

@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    settingsViewModel: SettingsViewModel,
    applicationState: ApplicationState
) {
    val currentTheme by settingsViewModel.currentTheme.collectAsState()
    val currentSorting by settingsViewModel.currentSorting.collectAsState()
    val showFab by settingsViewModel.showFab.collectAsState()
    val syncOnStartup by settingsViewModel.syncOnResume.collectAsState()
    val syncOnlyOnWifi by settingsViewModel.syncOnlyOnWifi.collectAsState()
    val syncOnlyWhenCharging by settingsViewModel.syncOnlyWhenCharging.collectAsState()
    val loadImageOnlyOnWifi by settingsViewModel.loadImageOnlyOnWifi.collectAsState()
    val showThumbnails by settingsViewModel.showThumbnails.collectAsState()
    val preloadCustomTab by settingsViewModel.preloadCustomTab.collectAsState()
    val maxItemsPerFeed by settingsViewModel.maximumCountPerFeed.collectAsState()
    val itemOpener by settingsViewModel.itemOpener.collectAsState()
    val linkOpener by settingsViewModel.linkOpener.collectAsState()
    val syncFrequency by settingsViewModel.syncFrequency.collectAsState()


    val context = LocalContext.current
    val powerManager = context.getSystemService<PowerManager>()

    // Each time activity is resumed (e.g. when returning from global settings)
    // recheck value for optimization
    val batteryOptimizationIgnored by applicationState.resumeTime
        .mapLatest {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        }
        .collectAsState(true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_activity_settings),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back button",
                        modifier = Modifier
                            .padding(start = upButtonStartPadding)
                            .clickable {
                                onNavigateUp()
                            }
                    )
                }
            )
        }
    ) { padding ->
        SettingsList(
            modifier = Modifier.padding(padding),
            currentThemeValue = currentTheme.asThemeOption(),
            onThemeChanged = { value ->
                settingsViewModel.setCurrentTheme(value.currentTheme)
            },
            currentSortingValue = currentSorting.asSortOption(),
            onSortingChanged = { value ->
                settingsViewModel.setCurrentSorting(value.currentSorting)
            },
            showFabValue = showFab,
            onShowFabChanged = { value ->
                settingsViewModel.setShowFab(value)
            },
            syncOnStartupValue = syncOnStartup,
            onSyncOnStartupChanged = {
                settingsViewModel.setSyncOnResume(it)
            },
            syncOnlyOnWifiValue = syncOnlyOnWifi,
            onSyncOnlyOnWifiChanged = {
                settingsViewModel.setSyncOnlyOnWifi(it)
            },
            syncOnlyWhenChargingValue = syncOnlyWhenCharging,
            onSyncOnlyWhenChargingChanged = {
                settingsViewModel.setSyncOnlyWhenCharging(it)
            },
            loadImageOnlyOnWifiValue = loadImageOnlyOnWifi,
            onLoadImageOnlyOnWifiChanged = {
                settingsViewModel.setLoadImageOnlyOnWifi(it)
            },
            showThumbnailsValue = showThumbnails,
            onShowThumbnailsChanged = {
                settingsViewModel.setShowThumbnails(it)
            },
            preloadCustomTabValue = preloadCustomTab,
            onPreloadCustomTabChanged = {
                settingsViewModel.setPreloadCustomTab(it)
            },
            maxItemsPerFeedValue = maxItemsPerFeed,
            onMaxItemsPerFeedChanged = {
                settingsViewModel.setMaxCountPerFeed(it)
            },
            currentItemOpenerValue = itemOpener,
            onItemOpenerChanged = {
                settingsViewModel.setItemOpener(it)
            },
            currentLinkOpenerValue = linkOpener,
            onLinkOpenerChanged = {
                settingsViewModel.setLinkOpener(it)
            },
            currentSyncFrequencyValue = syncFrequency,
            onSyncFrequencyChanged = {
                settingsViewModel.setSyncFrequency(it)
            },
            batteryOptimizationIgnoredValue = batteryOptimizationIgnored
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
                currentSortingValue = SortingOptions.NEWEST_FIRST.asSortOption(),
                onSortingChanged = {},
                showFabValue = true,
                onShowFabChanged = {},
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
                preloadCustomTabValue = true,
                onPreloadCustomTabChanged = {},
                maxItemsPerFeedValue = 101,
                onMaxItemsPerFeedChanged = {},
                currentItemOpenerValue = ItemOpener.CUSTOM_TAB,
                onItemOpenerChanged = {},
                currentLinkOpenerValue = LinkOpener.DEFAULT_BROWSER,
                onLinkOpenerChanged = {},
                currentSyncFrequencyValue = SyncFrequency.EVERY_12_HOURS,
                onSyncFrequencyChanged = {},
                batteryOptimizationIgnoredValue = false
            )
        }
    }
}

@Composable
fun SettingsList(
    modifier: Modifier,
    currentThemeValue: ThemeOption,
    onThemeChanged: (ThemeOption) -> Unit,
    currentSortingValue: SortOption,
    onSortingChanged: (SortOption) -> Unit,
    showFabValue: Boolean,
    onShowFabChanged: (Boolean) -> Unit,
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
    preloadCustomTabValue: Boolean,
    onPreloadCustomTabChanged: (Boolean) -> Unit,
    maxItemsPerFeedValue: Int,
    onMaxItemsPerFeedChanged: (Int) -> Unit,
    currentItemOpenerValue: ItemOpener,
    onItemOpenerChanged: (ItemOpener) -> Unit,
    currentLinkOpenerValue: LinkOpener,
    onLinkOpenerChanged: (LinkOpener) -> Unit,
    currentSyncFrequencyValue: SyncFrequency,
    onSyncFrequencyChanged: (SyncFrequency) -> Unit,
    batteryOptimizationIgnoredValue: Boolean
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(horizontal = contentHorizontalPadding)
            .verticalScroll(scrollState)
    ) {
        MenuSetting(
            currentValue = currentThemeValue,
            values = listOf(
                ThemeOptions.SYSTEM.asThemeOption(),
                ThemeOptions.DAY.asThemeOption(),
                ThemeOptions.NIGHT.asThemeOption()
            ),
            title = stringResource(id = R.string.theme),
            onSelection = onThemeChanged
        )

        MenuSetting(
            currentValue = currentSortingValue,
            values = listOf(
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

        Divider()

        GroupTitle {
            Text(stringResource(id = R.string.synchronization))
        }

        MenuSetting(
            currentValue = currentSyncFrequencyValue.asSyncFreqOption(),
            values = SyncFrequency.values().map {
                it.asSyncFreqOption()
            },
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
            values = listOf(
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

        Divider()

        GroupTitle {
            Text(stringResource(id = R.string.image_loading))
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

        Divider()

        GroupTitle {
            Text(stringResource(id = R.string.reader_settings))
        }

        MenuSetting(
            currentValue = currentItemOpenerValue.asItemOpenerOption(),
            values = listOf(
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
            values = listOf(
                LinkOpener.CUSTOM_TAB.asLinkOpenerOption(),
                LinkOpener.DEFAULT_BROWSER.asLinkOpenerOption()
            ),
            title = stringResource(id = R.string.open_links_with),
            onSelection = {
                onLinkOpenerChanged(it.linkOpener)
            }
        )

        SwitchSetting(
            checked = preloadCustomTabValue,
            onCheckedChanged = onPreloadCustomTabChanged,
            title = stringResource(id = R.string.preload_custom_tab)
        )
    }
}

@Composable
fun GroupTitle(
    title: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
        )
        Box(
            modifier = Modifier
                .height(64.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            ProvideTextStyle(
                value = MaterialTheme.typography.caption.merge(
                    TextStyle(color = MaterialTheme.colors.primary)
                )
            ) {
                title()
            }
        }
    }
}

@Composable
fun ExternalSetting(
    currentValue: String,
    title: String,
    icon: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
    currentValue: T,
    values: Iterable<T>,
    title: String,
    onSelection: (T) -> Unit,
    icon: @Composable () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
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
            for (value in values) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelection(value)
                    }
                ) {
                    val style = if (value == currentValue) {
                        MaterialTheme.typography.body1.merge(
                            TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.secondary
                            )
                        )
                    } else {
                        MaterialTheme.typography.body1
                    }
                    Text(
                        value.toString(),
                        style = style
                    )
                }
            }
        }
    }
}

@Composable
fun SwitchSetting(
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    title: String,
    icon: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChanged(!checked) },
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
            }
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChanged
        )
    }
}

@Composable
private fun RowScope.TitleAndSubtitle(
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.subtitle1) {
            title()
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.size(2.dp))
            ProvideTextStyle(value = MaterialTheme.typography.caption) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    content = subtitle
                )
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
    val name: String
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
    val name: String
) {
    override fun toString() = name
}

@Immutable
data class SyncFreqOption(
    val syncFrequency: SyncFrequency,
    val name: String
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
    val name: String
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
    val name: String
) {
    override fun toString() = name
}

@Composable
fun LinkOpener.asLinkOpenerOption() =
    LinkOpenerOption(
        linkOpener = this,
        name = stringResource(id = stringId)
    )
