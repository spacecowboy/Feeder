package com.nononsenseapps.feeder.ui.compose.readaloud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.AppSetting
import com.nononsenseapps.feeder.model.ForcedAuto
import com.nononsenseapps.feeder.model.ForcedLocale
import com.nononsenseapps.feeder.model.LocaleOverride
import com.nononsenseapps.feeder.model.ReadAloudProgress
import com.nononsenseapps.feeder.ui.compose.components.PaddedBottomAppBar
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.feed.PlainTooltipBox
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.onKeyEventLikeEscape
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

@Composable
fun HideableTTSPlayer(
    visibleState: MutableTransitionState<Boolean>,
    currentlyPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSkipNext: () -> Unit,
    languages: ImmutableHolder<List<Locale>>,
    onSelectLanguage: (LocaleOverride) -> Unit,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable (() -> Unit)? = null,
    progress: StateFlow<ReadAloudProgress>? = null,
    onSeekTo: (Long) -> Unit = {},
    onGoToSpokenText: () -> Unit = {},
    displayedArticleId: Long? = null,
) {
    AnimatedVisibility(
        modifier = modifier,
        visibleState = visibleState,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(256)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(256)),
    ) {
        val currentProgress = progress?.collectAsStateWithLifecycle()?.value ?: ReadAloudProgress()
        TTSPlayer(
            currentlyPlaying = currentlyPlaying,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onSkipNext = onSkipNext,
            languages = languages,
            onSelectLanguage = onSelectLanguage,
            floatingActionButton = floatingActionButton,
            progress = currentProgress,
            onSeekTo = onSeekTo,
            onGoToSpokenText = onGoToSpokenText,
            displayedArticleId = displayedArticleId,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSPlayer(
    currentlyPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSkipNext: () -> Unit,
    languages: ImmutableHolder<List<Locale>>,
    onSelectLanguage: (LocaleOverride) -> Unit,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable (() -> Unit)? = null,
    progress: ReadAloudProgress = ReadAloudProgress(),
    onSeekTo: (Long) -> Unit = {},
    onGoToSpokenText: () -> Unit = {},
    displayedArticleId: Long? = null,
) {
    var showMenu by remember {
        mutableStateOf(false)
    }
    val closeMenuText = stringResource(id = R.string.close_menu)
    val positionLabel = stringResource(R.string.read_aloud_position)
    Column(modifier = modifier) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val preparing = progress.preparationProgress
                var scrubPosition by remember(progress.articleId, preparing != null, progress.durationMillis) { mutableStateOf<Float?>(null) }
                if (preparing != null) {
                    Text(stringResource(R.string.read_aloud_preparing), style = MaterialTheme.typography.labelMedium)
                    LinearProgressIndicator(progress = { preparing }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
                } else {
                    val seekableDuration = if (progress.isDurationEstimated) progress.generatedMillis else progress.durationMillis
                    Slider(
                        value = scrubPosition ?: progress.positionMillis.toFloat(),
                        onValueChange = { scrubPosition = it.coerceAtMost(seekableDuration.toFloat()) },
                        onValueChangeFinished = {
                            scrubPosition?.let { onSeekTo(it.toLong()) }
                            scrubPosition = null
                        },
                        valueRange = 0f..progress.durationMillis.coerceAtLeast(1).toFloat(),
                        enabled = progress.durationMillis > 0 && progress.error == null,
                        modifier = Modifier.fillMaxWidth().safeSemantics { contentDescription = positionLabel },
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatSpeechTime(scrubPosition?.toLong() ?: progress.positionMillis), style = MaterialTheme.typography.labelMedium)
                        Text(
                            if (progress.isDurationEstimated) {
                                stringResource(
                                    R.string.read_aloud_estimated_time,
                                    (progress.synthesisProgress * 100).toInt().coerceIn(0, 99),
                                    formatSpeechTime(progress.durationMillis),
                                )
                            } else {
                                formatSpeechTime(progress.durationMillis)
                            },
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                progress.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium) }
            }
        }
        PaddedBottomAppBar(
            floatingActionButton = floatingActionButton,
            actions = {
                PlainTooltipBox(tooltip = { Text(stringResource(R.string.stop_reading)) }) {
                    IconButton(
                        onClick = onStop,
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = stringResource(R.string.stop_reading),
                        )
                    }
                }
                Crossfade(targetState = currentlyPlaying) { playing ->
                    if (playing) {
                        PlainTooltipBox(tooltip = { Text(stringResource(R.string.pause_reading)) }) {
                            IconButton(
                                onClick = onPause,
                            ) {
                                Icon(
                                    Icons.Default.Pause,
                                    contentDescription = stringResource(R.string.pause_reading),
                                )
                            }
                        }
                    } else {
                        PlainTooltipBox(tooltip = { Text(stringResource(R.string.resume_reading)) }) {
                            IconButton(
                                onClick = onPlay,
                            ) {
                                Icon(
                                    // TextToSpeech
                                    Icons.Default.PlayArrow,
                                    contentDescription = stringResource(R.string.resume_reading),
                                )
                            }
                        }
                    }
                }
                PlainTooltipBox(tooltip = { Text(stringResource(R.string.skip_to_next)) }) {
                    IconButton(
                        onClick = onSkipNext,
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = stringResource(R.string.skip_to_next),
                        )
                    }
                }
                Box {
                    PlainTooltipBox(tooltip = { Text(stringResource(R.string.set_language)) }) {
                        IconButton(
                            onClick = {
                                showMenu = true
                            },
                        ) {
                            Icon(
                                Icons.Default.Translate,
                                contentDescription = stringResource(R.string.set_language),
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier =
                            Modifier.onKeyEventLikeEscape {
                                showMenu = false
                            },
                    ) {
                        // Hidden button for TalkBack
                        DropdownMenuItem(
                            onClick = {
                                showMenu = false
                            },
                            text = {},
                            modifier =
                                Modifier
                                    .height(0.dp)
                                    .safeSemantics {
                                        contentDescription = closeMenuText
                                        role = Role.Button
                                    },
                        )
                        DropdownMenuItem(
                            onClick = {
                                onSelectLanguage(AppSetting)
                                showMenu = false
                            },
                            text = {
                                Text(stringResource(id = R.string.use_app_default))
                            },
                        )
                        DropdownMenuItem(
                            onClick = {
                                onSelectLanguage(ForcedAuto)
                                showMenu = false
                            },
                            text = {
                                Text(stringResource(id = R.string.use_detect_language))
                            },
                        )
                        HorizontalDivider()
                        for (lang in languages.item) {
                            DropdownMenuItem(
                                onClick = {
                                    onSelectLanguage(ForcedLocale(lang))
                                    showMenu = false
                                },
                                text = {
                                    Text(text = lang.getDisplayName(lang))
                                },
                            )
                        }
                    }
                }
                PlainTooltipBox(tooltip = { Text(stringResource(R.string.go_to_spoken_text)) }) {
                    val followingHere = progress.followSpokenText && progress.articleId != null && progress.articleId == displayedArticleId
                    IconButton(
                        onClick = onGoToSpokenText,
                        enabled = progress.articleId != null,
                        colors =
                            if (followingHere) {
                                IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            } else {
                                IconButtonDefaults.iconButtonColors()
                            },
                        modifier = Modifier.safeSemantics { selected = followingHere },
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.go_to_spoken_text))
                    }
                }
            },
        )
    }
}

internal fun formatSpeechTime(millis: Long): String {
    val seconds = millis.coerceAtLeast(0) / 1000
    return if (seconds >= 3600) {
        "%d:%02d:%02d".format(seconds / 3600, seconds / 60 % 60, seconds % 60)
    } else {
        "%d:%02d".format(seconds / 60, seconds % 60)
    }
}

@Preview
@Composable
private fun PlayerPreview() {
    PreviewTheme {
        TTSPlayer(
            currentlyPlaying = true,
            onPlay = {},
            onPause = {},
            onStop = {},
            onSkipNext = {},
            languages = ImmutableHolder(emptyList()),
            {},
        )
    }
}

@Preview
@Composable
private fun PlayerPreviewWithFab() {
    PreviewTheme {
        TTSPlayer(
            currentlyPlaying = true,
            onPlay = {},
            onPause = {},
            onStop = {},
            onSkipNext = {},
            languages = ImmutableHolder(emptyList()),
            {},
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                ) {
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = stringResource(R.string.mark_all_as_read),
                    )
                }
            },
        )
    }
}
