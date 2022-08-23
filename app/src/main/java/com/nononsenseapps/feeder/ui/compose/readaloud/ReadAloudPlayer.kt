package com.nononsenseapps.feeder.ui.compose.readaloud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.components.PaddedBottomAppBar
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme

@Composable
fun HideableReadAloudPlayer(
    visibleState: MutableTransitionState<Boolean>,
    currentlyPlaying: Boolean,
    floatingActionButton: @Composable (() -> Unit)? = null,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSkipNext: () -> Unit,
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
            onSkipNext = onSkipNext,
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
    onSkipNext: () -> Unit,
) {
    PaddedBottomAppBar(
        floatingActionButton = floatingActionButton,
        actions = {
            IconButton(
                onClick = onStop
            ) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = stringResource(R.string.stop_reading)
                )
            }
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
                onClick = onSkipNext
            ) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = stringResource(R.string.skip_to_next)
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
            onSkipNext = {},
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
            onSkipNext = {},
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                ) {
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = stringResource(R.string.mark_all_as_read)
                    )
                }
            }
        )
    }
}
