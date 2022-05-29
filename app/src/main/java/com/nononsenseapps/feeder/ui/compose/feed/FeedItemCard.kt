package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
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
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleTextStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens

@Composable
fun FeedItemCard(
    item: FeedListItem,
    showThumbnail: Boolean,
    imagePainter: @Composable (String) -> Unit,
    modifier: Modifier = Modifier,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleBookmarked: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
) {
    Card(
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
                    imagePainter(imageUrl)
                }
            }
            val titleAlpha = if (item.shouldBeShownAsUnread) {
                ContentAlpha.high
            } else {
                ContentAlpha.medium
            }
            CompositionLocalProvider(LocalContentAlpha provides titleAlpha) {
                Text(
                    text = item.title,
                    style = FeedListItemTitleTextStyle(),
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                )
            }
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
                        }
                    ) {
                        Text(
                            text = stringResource(
                                when (item.pinned) {
                                    true -> R.string.unpin_article
                                    false -> R.string.pin_article
                                }
                            )
                        )
                    }
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onToggleBookmarked()
                        }
                    ) {
                        Text(
                            text = stringResource(
                                when (item.bookmarked) {
                                    true -> R.string.remove_bookmark
                                    false -> R.string.bookmark_article
                                }
                            )
                        )
                    }
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onMarkAboveAsRead()
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.mark_items_above_as_read)
                        )
                    }
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onMarkBelowAsRead()
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.mark_items_below_as_read)
                        )
                    }
                    DropdownMenuItem(
                        onClick = {
                            onDismissDropdown()
                            onShareItem()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.share)
                        )
                    }
                }
            }
            CompositionLocalProvider(LocalContentAlpha provides titleAlpha) {
                Text(
                    text = item.snippet,
                    style = FeedListItemStyle(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 4,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                )
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
        imagePainter = {},
        onMarkAboveAsRead = {},
        onMarkBelowAsRead = {},
        onShareItem = {},
        onTogglePinned = {},
        onToggleBookmarked = {},
        dropDownMenuExpanded = false,
        onDismissDropdown = {}
    )
}
