package com.nononsenseapps.feeder.ui.compose.readaloud

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.mediumDateTimeFormat
import com.nononsenseapps.feeder.ui.compose.coil.RestrainedCropScaling
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.text.WithBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemDateStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemFeedTitleStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import com.nononsenseapps.feeder.ui.compose.utils.immutableListHolderOf
import java.time.LocalDate

@Composable
fun PlayListView(
    playedSeconds: Int,
    totalSeconds: Int,
    currentlyPlayingId: Long,
    currentlyPlaying: Boolean,
    articlesInPlaylist: ImmutableHolder<List<PlayableArticle>>,
    onSkipTo: (seconds: Float) -> Unit,
    onClickSpeed: () -> Unit,
    onClickSkipBack: () -> Unit,
    onClickTogglePlay: (articleId: Long, play: Boolean) -> Unit,
    onClickSkipForward: () -> Unit,
    onClickMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO a max size
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxSize()
                .then(modifier),
    ) {
        PlayerView(
            playedSeconds = playedSeconds,
            totalSeconds = totalSeconds,
            onSkipTo = onSkipTo,
            currentlyPlaying = currentlyPlaying,
            onClickSpeed = onClickSpeed,
            onClickSkipBack = onClickSkipBack,
            onClickTogglePlay = { play ->
                onClickTogglePlay(currentlyPlayingId, play)
            },
            onClickSkipForward = onClickSkipForward,
            onClickMarkAsRead = onClickMarkAsRead,
        )
        Divider()
        ArticleList(
            articles = articlesInPlaylist,
            onClickTogglePlay = onClickTogglePlay,
        )
    }
}

@Composable
fun PlayerView(
    playedSeconds: Int,
    totalSeconds: Int,
    onSkipTo: (seconds: Float) -> Unit,
    currentlyPlaying: Boolean,
    onClickSpeed: () -> Unit,
    onClickSkipBack: () -> Unit,
    onClickTogglePlay: (play: Boolean) -> Unit,
    onClickSkipForward: () -> Unit,
    onClickMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.then(modifier),
    ) {
        ArticleImage(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(150.dp),
        )
        Divider()
        PlayProgress(playedSeconds, totalSeconds, onSlide = onSkipTo)
        PlayButtons(
            currentlyPlaying = currentlyPlaying,
            onClickSpeed = onClickSpeed,
            onClickSkipBack = onClickSkipBack,
            onClickTogglePlay = onClickTogglePlay,
            onClickSkipForward = onClickSkipForward,
            onClickMarkAsRead = onClickMarkAsRead,
        )
    }
}

@Composable
fun ArticleList(
    articles: ImmutableHolder<List<PlayableArticle>>,
    onClickTogglePlay: (articleId: Long, play: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO drag n drop support
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxSize()
                .then(modifier),
    ) {
        items(
            count = articles.item.size,
            key = { index -> articles.item[index].id },
        ) {
            ArticleItem(
                article = articles.item[it],
                onClickTogglePlay = { play ->
                    onClickTogglePlay(articles.item[it].id, play)
                },
            )
            Divider(modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun ArticleItem(
    article: PlayableArticle,
    onClickTogglePlay: (play: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    WithBidiDeterminedLayoutDirection(paragraph = article.title) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(modifier),
        ) {
            ArticleImage(
                modifier =
                    Modifier
                        .size(64.dp),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = article.pubDate.format(mediumDateTimeFormat),
                        maxLines = 1,
                        style = FeedListItemDateStyle(),
                    )
                    Divider(modifier = Modifier.size(2.dp))
                    Text(
                        text = article.feedName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = FeedListItemFeedTitleStyle(),
                    )
                }
                Text(
                    text = article.title,
                    maxLines = 2,
                    style = FeedListItemTitleTextStyle(),
                    overflow = TextOverflow.Ellipsis,
                )
                PlayedProgress(article.playedSeconds, article.totalSeconds)
            }
            // TODO states
            IconButton(onClick = {
                onClickTogglePlay(!article.currentlyPlaying)
            }) {
                Icon(
                    if (article.currentlyPlaying) {
                        Icons.Default.PauseCircleOutline
                    } else {
                        Icons.Default.PlayCircleOutline
                    },
                    contentDescription = "TODO",
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
}

@Composable
fun PlayedProgress(
    playedSeconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
) {
    val totalText =
        remember(totalSeconds) {
            secondsToFormattedString(totalSeconds)
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .then(modifier),
    ) {
        if (playedSeconds > 0) {
            val playedText =
                remember(playedSeconds) {
                    secondsToFormattedString(playedSeconds)
                }
            val progress =
                remember(playedSeconds, totalSeconds) {
                    playedSeconds.toFloat() / totalSeconds
                }

            Text(text = playedText, maxLines = 1, style = FeedListItemDateStyle())
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.weight(1f),
            )
        }
        Text(text = totalText, maxLines = 1, style = FeedListItemDateStyle())
    }
}

private fun secondsToFormattedString(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
//    return if (h > 0) {
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
//    } else {
//        "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
//    }
}

@Composable
fun ArticleImage(modifier: Modifier = Modifier) {
    val pixelDensity = LocalDensity.current.density
    val contentScale =
        remember(pixelDensity) {
            RestrainedCropScaling(pixelDensity)
        }
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier.then(modifier),
    ) {
        val pixels =
            with(LocalDensity.current) {
                Size(maxWidth.roundToPx(), maxHeight.roundToPx())
            }
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data("https://cowboyprogrammer.org/images/2017/10/gimp_image_mode_index.png")
                    .listener(
                        onError = { a, b ->
                            Log.e("FEEDER_PLAYLIST", "error ${a.data}", b.throwable)
                        },
                    )
                    .scale(Scale.FIT)
                    .size(pixels)
                    .precision(Precision.INEXACT)
                    .build(),
            placeholder = rememberTintedVectorPainter(Icons.Outlined.Terrain),
            error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
            contentDescription = stringResource(id = R.string.article_image),
            contentScale = contentScale,
            alignment = Alignment.Center,
            modifier =
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .safeSemantics {
                        testTag = "article_image"
                    },
        )
    }
}

@Composable
fun PlayButtons(
    currentlyPlaying: Boolean,
    onClickSpeed: () -> Unit,
    onClickSkipBack: () -> Unit,
    onClickTogglePlay: (play: Boolean) -> Unit,
    onClickSkipForward: () -> Unit,
    onClickMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(modifier),
    ) {
        IconButton(onClick = onClickSpeed) {
            Icon(
                Icons.Default.Speed,
                contentDescription = "TODO",
                modifier = Modifier.size(36.dp),
            )
        }
        IconButton(onClick = onClickSkipBack) {
            Icon(
                Icons.Default.Replay10,
                contentDescription = "TODO",
                modifier = Modifier.size(36.dp),
            )
        }
        IconButton(
            onClick = {
                onClickTogglePlay(!currentlyPlaying)
            },
            modifier = Modifier.size(64.dp),
        ) {
            Icon(
                if (currentlyPlaying) {
                    Icons.Default.PauseCircleOutline
                } else {
                    Icons.Default.PlayCircleOutline
                },
                contentDescription = "TODO",
                modifier = Modifier.size(64.dp),
            )
        }
        IconButton(onClick = onClickSkipForward) {
            Icon(
                Icons.Default.Forward10,
                contentDescription = "TODO",
                modifier = Modifier.size(36.dp),
            )
        }
        IconButton(onClick = onClickMarkAsRead) {
            Icon(
                Icons.Default.Done,
                contentDescription = "TODO",
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Composable
fun PlayProgress(
    playedSeconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    onSlide: (Float) -> Unit,
) {
    val playedText =
        remember(playedSeconds) {
            secondsToFormattedString(playedSeconds)
        }
    val totalText =
        remember(totalSeconds) {
            secondsToFormattedString(totalSeconds)
        }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(modifier),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = playedText)
            Text(text = totalText)
        }
        Slider(value = playedSeconds.toFloat(), valueRange = 0f..totalSeconds.toFloat(), onValueChange = onSlide)
    }
}

@Preview
@Composable
private fun PreviewPlayListView() {
    FeederTheme {
        Surface {
            val articles =
                remember {
                    immutableListHolderOf(
                        PlayableArticle(
                            id = 1,
                            title = "Article 1",
                            feedName = "Feed 1",
                            imageUrl = "https://cowboyprogrammer.org/images/2017/10/gimp_image_mode_index.png",
                            playedSeconds = 0,
                            totalSeconds = 600,
                            pubDate = LocalDate.of(2021, 1, 1),
                            currentlyPlaying = false,
                        ),
                        PlayableArticle(
                            id = 2,
                            title = "Article 2",
                            feedName = "Feed 2 is also quite long which makes it overflow",
                            imageUrl = "https://cowboyprogrammer.org/images/2017/10/gimp_image_mode_index.png",
                            playedSeconds = 9,
                            totalSeconds = 59,
                            pubDate = LocalDate.of(2021, 2, 3),
                            currentlyPlaying = true,
                        ),
                        PlayableArticle(
                            id = 3,
                            title = "Article 3 with some longer text which makes it two lines and maybe even beyond",
                            feedName = "Feed 3",
                            imageUrl = "https://cowboyprogrammer.org/images/2017/10/gimp_image_mode_index.png",
                            playedSeconds = 79,
                            totalSeconds = 3788,
                            pubDate = LocalDate.of(2021, 4, 14),
                            currentlyPlaying = false,
                        ),
                    )
                }

            PlayListView(
                playedSeconds = 105,
                totalSeconds = 602,
                currentlyPlaying = true,
                currentlyPlayingId = 1L,
                articlesInPlaylist = articles,
                onSkipTo = { },
                onClickSpeed = { },
                onClickSkipBack = { },
                onClickTogglePlay = { _, _ -> },
                onClickSkipForward = { },
                onClickMarkAsRead = { },
            )
        }
    }
}
