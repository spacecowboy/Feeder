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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.model.PlaybackStatus
import com.nononsenseapps.feeder.model.TextToSpeechViewModel
import com.nononsenseapps.feeder.ui.compose.theme.ReadAloudPlayerStyle

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HideableReadAloudPlayer(
    readAloudViewModel: TextToSpeechViewModel
) {
    val readAloudState by readAloudViewModel.readAloudState

    AnimatedVisibility(
        visible = readAloudState != PlaybackStatus.STOPPED,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it * 2 }),
        exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut()
    ) {
        ReadAloudPlayer(readAloudViewModel = readAloudViewModel)
    }
}

@Composable
fun ReadAloudPlayer(
    readAloudViewModel: TextToSpeechViewModel
) {
    val title by readAloudViewModel.title
    val playbackStatus by readAloudViewModel.readAloudState
    ReadAloudPlayer(
        currentlyPlaying = playbackStatus == PlaybackStatus.PLAYING,
        title = title,
        onPlay = readAloudViewModel::play,
        onPause = readAloudViewModel::pause,
        onStop = readAloudViewModel::stop
    )
}

@Preview(showBackground = true)
@Composable
fun ReadAloudPlayer(
    currentlyPlaying: Boolean = true,
    title: String = "Article title",
    onPlay: () -> Unit = {},
    onPause: () -> Unit = {},
    onStop: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Max)
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
                        contentDescription = "Pause read aloud"
                    )
                }
            } else {
                IconButton(onClick = onPlay) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Resume read aloud"
                    )
                }
            }
        }
        IconButton(onClick = onStop) {
            Icon(
                Icons.Default.Stop,
                contentDescription = "Stop read aloud"
            )
        }
    }
}
