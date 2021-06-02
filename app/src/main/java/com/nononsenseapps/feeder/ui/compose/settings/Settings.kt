package com.nononsenseapps.feeder.ui.compose.settings

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.util.CurrentSorting
import com.nononsenseapps.feeder.util.CurrentTheme

@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    settingsViewModel: SettingsViewModel
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
                            .padding(start = 4.dp)
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
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SettingsScreenPreview() {
    FeederTheme(CurrentTheme.DAY) {
        Surface {
            SettingsList(
                modifier = Modifier,
                currentThemeValue = CurrentTheme.SYSTEM.asThemeOption(),
                onThemeChanged = {},
                currentSortingValue = CurrentSorting.NEWEST_FIRST.asSortOption(),
                onSortingChanged = {},
                showFabValue = true,
                onShowFabChanged = {},
                syncOnStartupValue = true,
                onSyncOnStartupChanged = {},
                syncOnlyOnWifiValue = true,
                syncOnlyWhenChargingValue = true,
                loadImageOnlyOnWifiValue = true,
                onLoadImageOnlyOnWifiChanged = {},
                onPreloadCustomTabChanged = {},
                onShowThumbnailsChanged = {},
                onSyncOnlyOnWifiChanged = {},
                onSyncOnlyWhenChargingChanged = {},
                preloadCustomTabValue = true,
                showThumbnailsValue = true,
                maxItemsPerFeedValue = 101,
                onMaxItemsPerFeedChanged = {}
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
    onMaxItemsPerFeedChanged: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
    ) {
        MenuSetting(
            currentValue = currentThemeValue,
            values = listOf(
                CurrentTheme.SYSTEM.asThemeOption(),
                CurrentTheme.DAY.asThemeOption(),
                CurrentTheme.NIGHT.asThemeOption()
            ),
            title = stringResource(id = R.string.theme),
            onSelection = onThemeChanged
        )

        MenuSetting(
            currentValue = currentSortingValue,
            values = listOf(
                CurrentSorting.NEWEST_FIRST.asSortOption(),
                CurrentSorting.OLDEST_FIRST.asSortOption()
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
            currentValue = stringResource(id = R.string.sync_option_every_3_hours),
            values = listOf(
                stringResource(id = R.string.sync_option_manually),
                stringResource(id = R.string.sync_option_every_15min),
                stringResource(id = R.string.sync_option_every_30min),
                stringResource(id = R.string.sync_option_every_hour),
                stringResource(id = R.string.sync_option_every_3_hours),
                stringResource(id = R.string.sync_option_every_6_hours),
                stringResource(id = R.string.sync_option_every_12_hours),
                stringResource(id = R.string.sync_option_every_day)
            ),
            title = stringResource(id = R.string.check_for_updates),
            onSelection = { TODO() }
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
            currentValue = stringResource(id = R.string.battery_optimization_enabled),
            title = stringResource(id = R.string.battery_optimization)
        ) {
            TODO()
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
            currentValue = stringResource(id = R.string.open_in_reader),
            values = listOf(
                stringResource(id = R.string.open_in_reader),
                stringResource(id = R.string.open_in_custom_tab),
                stringResource(id = R.string.open_in_default_browser),
                stringResource(id = R.string.open_in_web_view)
            ),
            title = stringResource(id = R.string.open_item_by_default_with),
            onSelection = { TODO() }
        )

        MenuSetting(
            currentValue = stringResource(id = R.string.open_in_custom_tab),
            values = listOf(
                stringResource(id = R.string.open_in_custom_tab),
                stringResource(id = R.string.open_in_default_browser),
                stringResource(id = R.string.open_in_webview)
            ),
            title = stringResource(id = R.string.open_links_with),
            onSelection = { TODO() }
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
    values: List<T>,
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
fun CurrentTheme.asThemeOption() =
    ThemeOption(
        currentTheme = this,
        name = stringResource(id = stringId)
    )

@Immutable
data class ThemeOption(
    val currentTheme: CurrentTheme,
    val name: String
) {
    override fun toString() = name
}

@Composable
fun CurrentSorting.asSortOption() =
    SortOption(
        currentSorting = this,
        name = stringResource(id = stringId)
    )

@Immutable
data class SortOption(
    val currentSorting: CurrentSorting,
    val name: String
) {
    override fun toString() = name
}
