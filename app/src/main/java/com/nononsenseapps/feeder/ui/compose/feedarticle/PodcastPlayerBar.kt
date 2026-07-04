package com.nononsenseapps.feeder.ui.compose.feedarticle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.PodcastPlayerState
import com.nononsenseapps.feeder.ui.compose.components.PaddedBottomAppBar
import com.nononsenseapps.feeder.ui.compose.feed.PlainTooltipBox

@Composable
fun HideablePodcastPlayer(
    visibleState: MutableTransitionState<Boolean>,
    viewState: PodcastPlayerState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekTo: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visibleState = visibleState,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(256)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(256)),
    ) {
        PodcastPlayerBar(
            viewState = viewState,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onSeekBack = onSeekBack,
            onSeekForward = onSeekForward,
            onSeekTo = onSeekTo,
        )
    }
}

@Composable
fun PodcastPlayerBar(
    viewState: PodcastPlayerState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekTo: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sliderPosition by remember(viewState.audioUrl) { mutableFloatStateOf(0f) }
    var isDragging by remember(viewState.audioUrl) { mutableStateOf(false) }
    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)

    LaunchedEffect(viewState.positionMillis, viewState.audioUrl, isDragging) {
        if (!isDragging) {
            sliderPosition = viewState.positionMillis.toFloat()
        }
    }

    Surface(
        modifier = modifier,
        color = containerColor,
        tonalElevation = 0.dp,
    ) {
        Column {
            if (viewState.isLoading || viewState.isBuffering) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (viewState.errorMessage == null) {
                Slider(
                    value = sliderPosition.coerceIn(0f, viewState.durationMillis.toFloat().coerceAtLeast(0f)),
                    onValueChange = {
                        isDragging = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        onSeekTo(sliderPosition.toInt())
                    },
                    valueRange = 0f..viewState.durationMillis.toFloat().coerceAtLeast(0f),
                    enabled = viewState.canSeek,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 8.dp),
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = formatPodcastDuration(viewState.positionMillis),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = formatPodcastDuration(viewState.durationMillis),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else {
                Text(
                    text = viewState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            PaddedBottomAppBar(
                containerColor = containerColor,
                tonalElevation = 0.dp,
                actions = {
                    PlainTooltipBox(tooltip = { Text(stringResource(R.string.stop_audio)) }) {
                        IconButton(onClick = onStop) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = stringResource(R.string.stop_audio),
                            )
                        }
                    }
                    Crossfade(targetState = viewState.isPlaying) { playing ->
                        if (playing) {
                            PlainTooltipBox(tooltip = { Text(stringResource(R.string.pause_audio)) }) {
                                IconButton(onClick = onPause) {
                                    Icon(
                                        Icons.Default.Pause,
                                        contentDescription = stringResource(R.string.pause_audio),
                                    )
                                }
                            }
                        } else {
                            PlainTooltipBox(tooltip = { Text(stringResource(R.string.play_audio)) }) {
                                IconButton(onClick = onPlay) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = stringResource(R.string.play_audio),
                                    )
                                }
                            }
                        }
                    }
                    PlainTooltipBox(tooltip = { Text(stringResource(R.string.seek_back_10_seconds)) }) {
                        IconButton(
                            onClick = onSeekBack,
                            enabled = viewState.canSeek,
                        ) {
                            Icon(
                                Icons.Default.Replay10,
                                contentDescription = stringResource(R.string.seek_back_10_seconds),
                            )
                        }
                    }
                    PlainTooltipBox(tooltip = { Text(stringResource(R.string.seek_forward_10_seconds)) }) {
                        IconButton(
                            onClick = onSeekForward,
                            enabled = viewState.canSeek,
                        ) {
                            Icon(
                                Icons.Default.Forward10,
                                contentDescription = stringResource(R.string.seek_forward_10_seconds),
                            )
                        }
                    }
                },
            )
        }
    }
}

private fun formatPodcastDuration(totalMillis: Int): String {
    val totalSeconds = (totalMillis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
