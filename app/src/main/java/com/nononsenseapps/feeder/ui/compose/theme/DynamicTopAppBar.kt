package com.nononsenseapps.feeder.ui.compose.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.text.withBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.utils.LocalWindowSize
import com.nononsenseapps.feeder.ui.compose.utils.WindowSize
import com.nononsenseapps.feeder.ui.compose.utils.localWindowSize

/**
 * On a small but tall screen this will be a LargeTopAppBar to make the screen
 * more one-hand friendly.
 *
 * One a short screen - or bigger tablet size - then it's a small top app bar which can scoll
 * out of the way to make best use of the available screen space.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    when (LocalWindowSize()) {
        WindowSize.CompactTall -> {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    withBidiDeterminedLayoutDirection(paragraph = title) {
                        Text(
                            title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = navigationIcon,
                actions = actions,
                modifier = modifier,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                ),
            )
        }
        else -> {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    withBidiDeterminedLayoutDirection(paragraph = title) {
                        Text(
                            title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = navigationIcon,
                actions = actions,
                modifier = modifier,
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                ),
            )
        }
    }
}

/**
 * Returns a scroll behavior suitable for top app bars used in DynamicTopAppBar.
 *
 * Large bars should not reenter when scrolling up but small ones should.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun dynamicScrollBehavior(
    topAppBarState: TopAppBarState = rememberTopAppBarState()
) =
    when (LocalWindowSize()) {
        WindowSize.CompactTall -> {
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
        }
        else -> {
            TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Tall phone",
    device = Devices.PIXEL_2,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun PreviewDynamicTopAppBarTall() {
    CompositionLocalProvider(localWindowSize provides WindowSize.CompactTall) {
        val scrollBehavior = dynamicScrollBehavior()
        FeederTheme {
            Scaffold(topBar = {
                DynamicTopAppBar(
                    title = "Top App Bar",
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.go_back)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    Text("Just a body")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Small phone",
    device = Devices.PIXEL_2,
    showBackground = true,
    widthDp = 350,
    heightDp = 500,
)
@Preview(
    name = "Tall phone landscape",
    device = Devices.PIXEL_2,
    showBackground = true,
    widthDp = 750,
    heightDp = 400,
)
@Preview(
    name = "Foldable light",
    device = Devices.FOLDABLE,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Tablet light",
    device = Devices.PIXEL_C,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun PreviewDynamicTopAppBarOther() {
    CompositionLocalProvider(localWindowSize provides WindowSize.CompactShort) {
        val scrollBehavior = dynamicScrollBehavior()
        FeederTheme {
            Scaffold(topBar = {
                DynamicTopAppBar(
                    title = "Top App Bar",
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.go_back)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    Text("Just a body")
                }
            }
        }
    }
}
