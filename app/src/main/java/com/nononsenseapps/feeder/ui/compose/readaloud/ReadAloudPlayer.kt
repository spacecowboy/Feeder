package com.nononsenseapps.feeder.ui.compose.readaloud

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.AppSetting
import com.nononsenseapps.feeder.model.ForcedAuto
import com.nononsenseapps.feeder.model.ForcedLocale
import com.nononsenseapps.feeder.model.LocaleOverride
import com.nononsenseapps.feeder.ui.compose.components.PaddedBottomAppBar
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import java.util.*

@Composable
fun HideableTTSPlayer(
    visibleState: MutableTransitionState<Boolean>,
    currentlyPlaying: Boolean,
    floatingActionButton: @Composable (() -> Unit)? = null,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSkipNext: () -> Unit,
    languages: ImmutableHolder<List<Locale>>,
    onSelectLanguage: (LocaleOverride) -> Unit,
) {
    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(256)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(256)),
    ) {
        TTSPlayer(
            currentlyPlaying = currentlyPlaying,
            floatingActionButton = floatingActionButton,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
            onSkipNext = onSkipNext,
            languages = languages,
            onSelectLanguage = onSelectLanguage,
        )
    }
}

@Composable
fun TTSPlayer(
    currentlyPlaying: Boolean,
    floatingActionButton: @Composable (() -> Unit)? = null,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSkipNext: () -> Unit,
    languages: ImmutableHolder<List<Locale>>,
    onSelectLanguage: (LocaleOverride) -> Unit,
) {
    var showMenu by remember {
        mutableStateOf(false)
    }
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
            Box {
                IconButton(
                    onClick = {
                        showMenu = true
                    }
                ) {
                    Icon(
                        Icons.Default.Translate,
                        contentDescription = stringResource(R.string.set_language)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onSelectLanguage(AppSetting)
                            showMenu = false
                        },
                        text = {
                            Text(stringResource(id = R.string.use_app_default))
                        }
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        DropdownMenuItem(
                            onClick = {
                                onSelectLanguage(ForcedAuto)
                                showMenu = false
                            },
                            text = {
                                Text(stringResource(id = R.string.use_detect_language))
                            }
                        )
                    }
                    Divider()
                    for (lang in languages.item) {
                        DropdownMenuItem(
                            onClick = {
                                onSelectLanguage(ForcedLocale(lang))
                                showMenu = false
                            },
                            text = {
                                Text(text = lang.getDisplayName(lang))
                            }
                        )
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun PlayerPreview() {
    FeederTheme {
        TTSPlayer(
            currentlyPlaying = true,
            onPlay = {},
            onPause = {},
            onStop = {},
            onSkipNext = {},
            onSelectLanguage = {},
            languages = ImmutableHolder(emptyList()),
        )
    }
}

@Preview
@Composable
fun PlayerPreviewWithFab() {
    FeederTheme {
        TTSPlayer(
            currentlyPlaying = true,
            onPlay = {},
            onPause = {},
            onStop = {},
            onSkipNext = {},
            onSelectLanguage = {},
            languages = ImmutableHolder(emptyList()),
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
