package com.nononsenseapps.feeder.ui.compose.filter

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.feed.FeedItemSuperCompact
import com.nononsenseapps.feeder.ui.compose.feed.FeedListItem
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.SensibleTopAppBar
import com.nononsenseapps.feeder.ui.compose.theme.SetStatusBarColorToMatchScrollableTopAppBar

@Composable
fun FilterDualScreen(
    onNavigateUp: () -> Unit,
    viewModel: FilterScreenViewModel,
) {
    val viewState by viewModel.viewState.collectAsState()

    FilterDualScreen(
        onNavigateUp = onNavigateUp,
        currentFilter = viewState.currentFilter,
        onFilterChange = viewModel::setFilter,

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDualScreen(
    onNavigateUp: () -> Unit,
    matchCount: Int = 0,
    totalCount: Int = 1,
    currentFilter: String = "a[bc]",
    onFilterChange: (String) -> Unit = {},
    filterHasError: Boolean = true,
    saveable: Boolean = false,
    onSave: () -> Unit = {},
    matchingFeedItems: List<FeedListItem> = emptyList(),
    @StringRes filterSupportText: Int? = null,
) {
    val dimens = LocalDimens.current

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    SetStatusBarColorToMatchScrollableTopAppBar(scrollBehavior)

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            SensibleTopAppBar(
                scrollBehavior = scrollBehavior,
                title = "Edit filter",
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.go_back),
                        )
                    }
                },
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = dimens.margin),
            contentAlignment = Alignment.TopCenter,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.gutter),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .width(dimens.maxContentWidth),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    FilterDescription()
                    FilterCounter(count = matchCount, total = totalCount)
                    FilterInput(
                        value = currentFilter,
                        onValueChange = onFilterChange,
                        isError = filterHasError,
                        supportText = filterSupportText,
                    )
                    FilterSaveButton(
                        enabled = saveable,
                        onSave = onSave,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        "Articles which will be hidden by this filter",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (matchingFeedItems.isNotEmpty()) {
                            item {
                                Divider(
                                    modifier = Modifier
                                        .height(1.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                        items(matchingFeedItems) { item ->
                            FeedItemSuperCompact(
                                item = item,
                                showThumbnail = false,
                                onMarkAboveAsRead = {},
                                onMarkBelowAsRead = {},
                                onShareItem = {},
                                newIndicator = false,
                                dropDownMenuExpanded = false,
                                onDismissDropdown = {},
                                onTogglePinned = {},
                                onToggleBookmarked = {},
                            )
                            Divider(
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterDescription() {
    Text(
        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc vel nisl interdum nisl scelerisque tempor. Praesent sagittis mauris a nisi vehicula tempus. Integer ut finibus sapien. Praesent vitae elementum massa. Mauris nec volutpat neque. Phasellus venenatis eu lectus ut vehicula. Aenean eu tortor vel ipsum pretium varius non eu nunc. Aliquam lacinia consequat viverra. Donec egestas fringilla tincidunt. Ut vestibulum purus in est facilisis, vel suscipit sapien pharetra. Integer consequat felis sed fermentum vehicula. Nullam lacus metus, viverra eu pretium ut, volutpat sed ipsum. Sed rhoncus auctor massa, a varius lorem sagittis nec. Praesent quis erat ante. Aenean ornare est.",
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
fun FilterCounter(
    count: Int,
    total: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Number of matches",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            "$count / $total",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterInput(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    @StringRes supportText: Int?,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text("Filter")
        },
        supportingText = supportText?.let { textId ->
            {
                Text(stringResource(id = textId))
            }
        },
        isError = isError,
        trailingIcon = {
            if (isError) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            } else {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                )
            }
        }
    )
}

@Composable
fun FilterSaveButton(
    enabled: Boolean,
    onSave: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = onSave,
            enabled = enabled,
        ) {
            Text(text = stringResource(id = R.string.save))
        }
    }
}

@Composable
@Preview(device = Devices.PIXEL_C)
fun PreviewTablet() {
    FeederTheme {
        FilterDualScreen(
            onNavigateUp = {},
            matchingFeedItems = listOf(
                FeedListItem(
                    1L,
                    "A very special item",
                    "Just a snippet",
                    "News Corp",
                    true,
                    "Yesterday",
                    null,
                    null,
                    false,
                    false,
                    null
                )
            ),
        )
    }
}
