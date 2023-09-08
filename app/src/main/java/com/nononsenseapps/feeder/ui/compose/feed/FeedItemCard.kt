package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.text.WithBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemDateStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemFeedTitleStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemSnippetTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.titleFontWeight
import com.nononsenseapps.feeder.ui.compose.utils.ThemePreviews
import com.nononsenseapps.feeder.ui.compose.utils.onKeyEventLikeEscape
import java.net.URL
import java.time.Instant
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@Composable
fun FeedItemCard(
    item: FeedListItem,
    showThumbnail: Boolean,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onToggleBookmarked: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
    bookmarkIndicator: Boolean,
    maxLines: Int,
    showOnlyTitle: Boolean,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .requiredHeightIn(min = minimumTouchSize),
        ) {
            if (showThumbnail) {
                item.imageUrl?.let { imageUrl ->
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val pixels = with(LocalDensity.current) {
                            val width = maxWidth.roundToPx()
                            Size(width, (width * 9) / 16)
                        }
                        val alpha = if (item.unread) {
                            1f
                        } else {
                            0.74f
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .listener(
                                    onError = { a, b ->
                                        Log.e("FEEDER_CARD", "error ${a.data}", b.throwable)
                                    },
                                )
                                .scale(Scale.FILL)
                                .size(pixels)
                                .precision(Precision.INEXACT)
                                .build(),
                            placeholder = rememberTintedVectorPainter(Icons.Outlined.Terrain),
                            error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                            contentDescription = stringResource(id = R.string.article_image),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .fillMaxWidth()
                                .aspectRatio(16.0f / 9.0f)
                                .alpha(alpha),
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 8.dp),
            ) {
                FeedItemEitherIndicator(
                    bookmarked = item.bookmarked && bookmarkIndicator,
                    itemImage = null,
                    feedImageUrl = item.feedImageUrl?.toHttpUrlOrNull(),
                    size = 16.dp,
                )
                FeedItemText(
                    item = item,
                    onMarkAboveAsRead = onMarkAboveAsRead,
                    onMarkBelowAsRead = onMarkBelowAsRead,
                    onShareItem = onShareItem,
                    onToggleBookmarked = onToggleBookmarked,
                    dropDownMenuExpanded = dropDownMenuExpanded,
                    onDismissDropdown = onDismissDropdown,
                    maxLines = maxLines,
                    showOnlyTitle = showOnlyTitle,
                )
            }
        }
    }
}

@Composable
fun RowScope.FeedItemText(
    item: FeedListItem,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onToggleBookmarked: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
    maxLines: Int,
    showOnlyTitle: Boolean,
    modifier: Modifier = Modifier,
) {
    val snippetStyle = FeedListItemSnippetTextStyle()
    val joinedText = remember(item, showOnlyTitle) {
        buildAnnotatedString {
            if (item.title.isNotBlank()) {
                append(item.title)
                if (!showOnlyTitle && item.snippet.isNotBlank()) {
                    withStyle(snippetStyle.toSpanStyle()) {
                        append('\n')
                        append(item.snippet)
                    }
                }
            } else {
                // Heard of one feed which did not have titles. If so always include snippet
                append(item.snippet)
            }
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .weight(1f),
    ) {
        WithBidiDeterminedLayoutDirection(paragraph = joinedText.text) {
            Text(
                text = joinedText,
                style = FeedListItemTitleTextStyle(),
                fontWeight = titleFontWeight(item.unread),
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLines,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
        // Want the dropdown to center on the middle text row
        Box {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    WithBidiDeterminedLayoutDirection(paragraph = item.feedTitle) {
                        Text(
                            text = item.feedTitle,
                            style = FeedListItemFeedTitleStyle(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f),
                        )
                    }
                    WithBidiDeterminedLayoutDirection(paragraph = item.pubDate) {
                        Text(
                            text = item.pubDate,
                            style = FeedListItemDateStyle(),
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier,
                        )
                    }
                }
            }
            DropdownMenu(
                expanded = dropDownMenuExpanded,
                onDismissRequest = onDismissDropdown,
                modifier = Modifier.onKeyEventLikeEscape(onDismissDropdown),
            ) {
                DropdownMenuItem(
                    onClick = {
                        onDismissDropdown()
                        onToggleBookmarked()
                    },
                    text = {
                        Text(
                            text = stringResource(
                                when (item.bookmarked) {
                                    true -> R.string.unsave_article
                                    false -> R.string.save_article
                                },
                            ),
                        )
                    },
                )
                DropdownMenuItem(
                    onClick = {
                        onDismissDropdown()
                        onMarkAboveAsRead()
                    },
                    text = {
                        Text(
                            text = stringResource(id = R.string.mark_items_above_as_read),
                        )
                    },
                )
                DropdownMenuItem(
                    onClick = {
                        onDismissDropdown()
                        onMarkBelowAsRead()
                    },
                    text = {
                        Text(
                            text = stringResource(id = R.string.mark_items_below_as_read),
                        )
                    },
                )
                DropdownMenuItem(
                    onClick = {
                        onDismissDropdown()
                        onShareItem()
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.share),
                        )
                    },
                )
            }
        }
    }
}

@Composable
@ThemePreviews
private fun Preview() {
    FeederTheme {
        FeedItemCard(
            item = FeedListItem(
                title = "title",
                snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                feedTitle = "Super Duper Feed One two three hup di too dasf dsaf asd fsa dfasdf",
                pubDate = "Jun 9, 2021",
                unread = true,
                imageUrl = null,
                link = null,
                id = ID_UNSET,
                bookmarked = true,
                feedImageUrl = null,
                primarySortTime = Instant.EPOCH,
                rawPubDate = null,
            ),
            showThumbnail = true,
            onMarkAboveAsRead = {},
            onMarkBelowAsRead = {},
            onShareItem = {},
            onToggleBookmarked = {},
            dropDownMenuExpanded = false,
            onDismissDropdown = {},
            maxLines = 2,
            showOnlyTitle = false,
            bookmarkIndicator = true,
        )
    }
}

@Composable
@ThemePreviews
private fun PreviewWithImageUnread() {
    FeederTheme {
        Box(
            modifier = Modifier.width((300 - 2 * 16).dp),
        ) {
            FeedItemCard(
                item = FeedListItem(
                    title = "title can be one line",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Feed",
                    pubDate = "Jun 9, 2021",
                    unread = true,
                    imageUrl = "blabla",
                    link = null,
                    id = ID_UNSET,
                    bookmarked = false,
                    feedImageUrl = URL("https://foo/bar.png"),
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                ),
                showThumbnail = true,
                onMarkAboveAsRead = {},
                onMarkBelowAsRead = {},
                onShareItem = {},
                onToggleBookmarked = {},
                dropDownMenuExpanded = false,
                onDismissDropdown = {},
                maxLines = 2,
                showOnlyTitle = false,
                bookmarkIndicator = true,
            )
        }
    }
}

@Composable
@ThemePreviews
private fun PreviewWithImageRead() {
    FeederTheme {
        Box(
            modifier = Modifier.width((300 - 2 * 16).dp),
        ) {
            FeedItemCard(
                item = FeedListItem(
                    title = "title can be one line",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed",
                    pubDate = "Jun 9, 2021",
                    unread = false,
                    imageUrl = "blabla",
                    link = null,
                    id = ID_UNSET,
                    bookmarked = true,
                    feedImageUrl = null,
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                ),
                showThumbnail = true,
                onMarkAboveAsRead = {},
                onMarkBelowAsRead = {},
                onShareItem = {},
                onToggleBookmarked = {},
                dropDownMenuExpanded = false,
                onDismissDropdown = {},
                maxLines = 2,
                showOnlyTitle = false,
                bookmarkIndicator = true,
            )
        }
    }
}
