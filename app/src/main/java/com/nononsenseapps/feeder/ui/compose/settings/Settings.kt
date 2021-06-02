package com.nononsenseapps.feeder.ui.compose.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.DIAwareViewModel
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.util.CurrentTheme
import kotlin.math.exp

@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    settingsViewModel: SettingsViewModel
) {
    val currentTheme by settingsViewModel.currentTheme.collectAsState()

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
                },
                actions = {
                    // todo
                }
            )
        }
    ) { padding ->
        SettingsList(
            modifier = Modifier.padding(padding),
            currentThemeValue = currentTheme.asThemeOption(),
            onThemeChanged = { value ->
                settingsViewModel.setCurrentTheme(value.currentTheme)
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
                onThemeChanged = {}
            )
        }
    }
}

@Composable
fun SettingsList(
    modifier: Modifier,
    currentThemeValue: ThemeOption,
    onThemeChanged: (ThemeOption) -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
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
            currentValue = stringResource(id = R.string.sort_newest_first),
            values = listOf(
                stringResource(id = R.string.sort_newest_first),
                stringResource(id = R.string.sort_oldest_first)
            ),
            title = stringResource(id = R.string.sort),
            onSelection = { TODO() }
        )

        SwitchSetting(
            checked = false,
            onCheckedChanged = { TODO() },
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
            checked = false,
            onCheckedChanged = { TODO() },
            title = stringResource(id = R.string.on_startup)
        )

        SwitchSetting(
            checked = false,
            onCheckedChanged = { TODO() },
            title = stringResource(id = R.string.only_on_wifi)
        )

        SwitchSetting(
            checked = false,
            onCheckedChanged = { TODO() },
            title = stringResource(id = R.string.only_when_charging)
        )

        MenuSetting(
            currentValue = 100,
            values = listOf(
                50,
                100,
                200,
                500,
                100
            ),
            title = stringResource(id = R.string.max_feed_items),
            onSelection = { TODO() }
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
            checked = false,
            onCheckedChanged = { TODO() },
            title = stringResource(id = R.string.only_on_wifi)
        )

        SwitchSetting(
            checked = false,
            onCheckedChanged = { TODO() },
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
            checked = false,
            onCheckedChanged = { TODO() },
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
