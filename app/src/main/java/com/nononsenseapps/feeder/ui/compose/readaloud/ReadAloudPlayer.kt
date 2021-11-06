package com.nononsenseapps.feeder.ui.compose.readaloud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.ReadAloudPlayerStyle

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HideableReadAloudPlayer(
    visible: Boolean,
    currentlyPlaying: Boolean,
    title: String,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it * 2 }),
        exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut()
    ) {
        ReadAloudPlayer(
            currentlyPlaying = currentlyPlaying,
            title = title,
            onPlay = onPlay,
            onPause = onPause,
            onStop = onStop,
        )
    }
}

@Composable
fun ReadAloudPlayer(
    currentlyPlaying: Boolean,
    title: String,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Max)
            .navigationBarsPadding()
    ) {
        Text(
            title,
            style = ReadAloudPlayerStyle(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(weight = 1.0f, fill = true)
                .padding(top = 4.dp, end = 4.dp, bottom = 4.dp, start = 16.dp)
        )
        Crossfade(targetState = currentlyPlaying) { playing ->
            if (playing) {
                IconButton(onClick = onPause) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = stringResource(R.string.pause_reading)
                    )
                }
            } else {
                IconButton(onClick = onPlay) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.resume_reading)
                    )
                }
            }
        }
        IconButton(onClick = onStop) {
            Icon(
                Icons.Default.Stop,
                contentDescription = stringResource(R.string.stop_reading)
            )
        }
    }
}

@Preview
@Composable
fun PlayerPreview() {
    FeederTheme {
        ReadAloudPlayer(
            currentlyPlaying = true,
            title = "Article Title",
            onPlay = {},
            onPause = {},
            onStop = {},
        )
    }
}
