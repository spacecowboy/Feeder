package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.text.WithBidiDeterminedLayoutDirection
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemDateStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemFeedTitleStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.titleFontWeight
import com.nononsenseapps.feeder.ui.compose.utils.ThemePreviews
import org.threeten.bp.Instant

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
    newIndicator: Boolean,
    bookmarkIndicator: Boolean,
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
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd,
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .listener(
                                    onError = { a, b ->
                                        Log.e("FEEDER_CARD", "error ${a.data}", b.throwable)
                                    },
                                )
                                .scale(Scale.FIT)
                                .size(1000)
                                .precision(Precision.INEXACT)
                                .build(),
                            placeholder = rememberTintedVectorPainter(Icons.Outlined.Terrain),
                            error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                            contentDescription = stringResource(id = R.string.article_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .fillMaxWidth()
                                .aspectRatio(16.0f / 9.0f),
                        )
                        FeedItemIndicatorRow(
                            unread = item.unread && newIndicator,
                            bookmarked = item.bookmarked && bookmarkIndicator,
                            modifier = Modifier.padding(
                                top = 12.dp,
                                end = 12.dp,
                            ),
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f, fill = true),
                ) {
                    WithBidiDeterminedLayoutDirection(paragraph = item.title) {
                        Text(
                            text = item.title,
                            style = FeedListItemTitleTextStyle(),
                            fontWeight = titleFontWeight(item.unread),
                            modifier = Modifier
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                                .fillMaxWidth(),
                        )
                    }
                    // Want the dropdown to center on the middle text row
                    Box {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                val text = buildAnnotatedString {
                                    if (item.pubDate.isNotBlank()) {
                                        append("${item.pubDate} ‧ ")
                                    }
                                    withStyle(FeedListItemFeedTitleStyle().toSpanStyle()) {
                                        append(item.feedTitle)
                                    }
                                }
                                WithBidiDeterminedLayoutDirection(paragraph = text.text) {
                                    Text(
                                        text = text,
                                        style = FeedListItemDateStyle(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp, end = 8.dp),
                                    )
                                }
                            }
                        }
                        DropdownMenu(
                            expanded = dropDownMenuExpanded,
                            onDismissRequest = onDismissDropdown,
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
//            Text(
//                text = item.snippet,
//                style = FeedListItemStyle(),
//                overflow = TextOverflow.Ellipsis,
//                maxLines = 4,
//                modifier = Modifier
//                    .padding(start = 8.dp, end = 8.dp)
//            )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                    )
                }
                if (!showThumbnail || item.imageUrl == null) {
                    FeedItemIndicatorColumn(
                        unread = item.unread && newIndicator,
                        bookmarked = item.bookmarked && bookmarkIndicator,
                        modifier = Modifier.padding(
                            top = 12.dp,
                            bottom = 12.dp,
                            end = 12.dp,
                        ),
                    )
                }
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
                feedTitle = "Super Duper Feed One two three hup di too dasf",
                pubDate = "Jun 9, 2021",
                unread = true,
                imageUrl = null,
                link = null,
                id = ID_UNSET,
                bookmarked = false,
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
            newIndicator = true,
            bookmarkIndicator = true,
        )
    }
}

@Composable
@ThemePreviews
private fun PreviewWithImage() {
    FeederTheme {
        Box(
            modifier = Modifier.width((300 - 2 * 16).dp),
        ) {
            FeedItemCard(
                item = FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = true,
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
                newIndicator = true,
                bookmarkIndicator = true,
            )
        }
    }
}
