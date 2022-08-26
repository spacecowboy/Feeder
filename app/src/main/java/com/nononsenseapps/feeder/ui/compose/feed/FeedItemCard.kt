package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.Image
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
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemDateStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemFeedTitleStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens

@Composable
fun FeedItemCard(
    item: FeedListItem,
    showThumbnail: Boolean,
    imagePainter: @Composable (String, Modifier) -> Unit,
    modifier: Modifier = Modifier,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleBookmarked: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier
            .padding(
                top = 8.dp,
                bottom = 8.dp,
                start = LocalDimens.current.margin,
                end = LocalDimens.current.margin
            )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .requiredHeightIn(min = minimumTouchSize)
        ) {
            if (showThumbnail) {
                item.imageUrl?.let { imageUrl ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd,
                    ) {
                        imagePainter(imageUrl, Modifier.clip(MaterialTheme.shapes.medium))
                        FeedItemIndicatorRow(
                            unread = item.unread,
                            bookmarked = item.bookmarked,
                            pinned = item.pinned,
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

                    Text(
                        text = item.title,
                        style = FeedListItemTitleTextStyle(),
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                    )
                    // Want the dropdown to center on the middle text row
                    Box {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text(
                                    text = buildAnnotatedString {
                                        if (item.pubDate.isNotBlank()) {
                                            append("${item.pubDate} ‧ ")
                                        }
                                        withStyle(FeedListItemFeedTitleStyle().toSpanStyle()) {
                                            append(item.feedTitle)
                                        }
                                    },
                                    style = FeedListItemDateStyle(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Clip,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp, end = 8.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = dropDownMenuExpanded,
                            onDismissRequest = onDismissDropdown
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onDismissDropdown()
                                    onTogglePinned()
                                },
                                text = {
                                    Text(
                                        text = stringResource(
                                            when (item.pinned) {
                                                true -> R.string.unpin_article
                                                false -> R.string.pin_article
                                            }
                                        )
                                    )
                                }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    onDismissDropdown()
                                    onToggleBookmarked()
                                },
                                text = {
                                    Text(
                                        text = stringResource(
                                            when (item.bookmarked) {
                                                true -> R.string.remove_bookmark
                                                false -> R.string.bookmark_article
                                            }
                                        )
                                    )
                                }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    onDismissDropdown()
                                    onMarkAboveAsRead()
                                },
                                text = {
                                    Text(
                                        text = stringResource(id = R.string.mark_items_above_as_read)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    onDismissDropdown()
                                    onMarkBelowAsRead()
                                },
                                text = {
                                    Text(
                                        text = stringResource(id = R.string.mark_items_below_as_read)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    onDismissDropdown()
                                    onShareItem()
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.share)
                                    )
                                }
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
                            .height(8.dp)
                    )
                }
                if (!showThumbnail || item.imageUrl == null) {
                    FeedItemIndicatorColumn(
                        unread = item.unread,
                        bookmarked = item.bookmarked,
                        pinned = item.pinned,
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
@Preview(showBackground = true)
private fun preview() {
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
            pinned = false,
            bookmarked = false,
        ),
        showThumbnail = true,
        imagePainter = { _, _ -> },
        onMarkAboveAsRead = {},
        onMarkBelowAsRead = {},
        onShareItem = {},
        onTogglePinned = {},
        onToggleBookmarked = {},
        dropDownMenuExpanded = false,
        onDismissDropdown = {}
    )
}

@Composable
@Preview(showBackground = true)
private fun previewWithImage() {
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
            pinned = true,
            bookmarked = true,
        ),
        showThumbnail = true,
        onMarkAboveAsRead = {},
        onMarkBelowAsRead = {},
        onShareItem = {},
        onTogglePinned = {},
        onToggleBookmarked = {},
        dropDownMenuExpanded = false,
        onDismissDropdown = {},
        imagePainter = { _, modifier ->
            Box {
                Image(
                    painter = painterResource(id = R.drawable.placeholder_image_article_day),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = modifier
                        .fillMaxWidth()
                        .aspectRatio(16.0f / 9.0f)
                )
            }
        }
    )
}
