package com.nononsenseapps.feeder.ui.compose.readaloud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.bottomBarHeight
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme

@Composable
fun HideableReadAloudPlayer(
    visibleState: MutableTransitionState<Boolean>,
    currentlyPlaying: Boolean,
    floatingActionButton: @Composable (() -> Unit)? = null,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
) {
    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(256)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(256)),
    ) {
        ReadAloudPlayer(
            currentlyPlaying = currentlyPlaying,
            floatingActionButton = floatingActionButton,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
        )
    }
}

@Composable
fun ReadAloudPlayer(
    currentlyPlaying: Boolean,
    floatingActionButton: @Composable (() -> Unit)? = null,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
) {
    BottomAppBar(
        floatingActionButton = floatingActionButton,
        actions = {
            Crossfade(targetState = currentlyPlaying) { playing ->
                if (playing) {
                    IconButton(
                        onClick = onPause
                    ) {
                        Icon(
                            Icons.Default.Pause,
                            contentDescription = stringResource(R.string.pause_reading)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onPlay
                    ) {
                        Icon(
                            // TextToSpeech
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.resume_reading)
                        )
                    }
                }
            }
            IconButton(
                onClick = onStop
            ) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = stringResource(R.string.stop_reading)
                )
            }
            // Make app bar as high as normally plus navigation bar
            Column {
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(bottomBarHeight - (2 * 4).dp) // top and bottom padding
                )
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
                )
            }
        }
    )
}

@Preview
@Composable
fun PlayerPreview() {
    FeederTheme {
        ReadAloudPlayer(
            currentlyPlaying = true,
            onPlay = {},
            onPause = {},
            onStop = {},
        )
    }
}

@Preview
@Composable
fun PlayerPreviewWithFab() {
    FeederTheme {
        ReadAloudPlayer(
            currentlyPlaying = true,
            onPlay = {},
            onPause = {},
            onStop = {},
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.mark_all_as_read)
                    )
                }
            }
        )
    }
}
