package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.db.room.FeedItemCursor
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.coil.rememberTintedVectorPainter
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import java.net.URL
import java.time.Instant
import java.time.ZonedDateTime

@Composable
fun FeedItemCompact(
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
    imageWidth: Dp = 64.dp,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .height(IntrinsicSize.Min)
            .padding(start = LocalDimens.current.margin),
    ) {
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
            modifier = Modifier
                .requiredHeightIn(min = minimumTouchSize)
                .padding(vertical = 8.dp),
        )

        if ((item.bookmarked && bookmarkIndicator) || showThumbnail && (item.imageUrl != null || item.feedImageUrl != null)) {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.TopEnd,
            ) {
                if (item.bookmarked && bookmarkIndicator) {
                    FeedItemEitherIndicator(
                        bookmarked = true,
                        itemImage = null,
                        feedImageUrl = null,
                        size = 24.dp,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(64.dp),
                    )
                } else {
                    (item.imageUrl ?: item.feedImageUrl?.toString())?.let { imageUrl ->
                        val scale = if (item.imageUrl != null) {
                            ContentScale.Crop
                        } else {
                            ContentScale.Fit
                        }
                        val pixels = with(LocalDensity.current) {
                            Size(64.dp.roundToPx(), 96.dp.roundToPx())
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .listener(
                                    onError = { a, b ->
                                        Log.e("FEEDER_COMPACT", "error ${a.data}", b.throwable)
                                    },
                                )
                                .scale(Scale.FILL)
                                .size(pixels)
                                .precision(Precision.INEXACT)
                                .build(),
                            placeholder = rememberTintedVectorPainter(Icons.Outlined.Terrain),
                            error = rememberTintedVectorPainter(Icons.Outlined.ErrorOutline),
                            contentDescription = stringResource(id = R.string.article_image),
                            contentScale = scale,
                            modifier = Modifier
                                .width(imageWidth)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        } else {
            // Taking Row spacing into account
            Spacer(modifier = Modifier.width(LocalDimens.current.margin - 4.dp))
        }
    }
}

@Immutable
data class FeedListItem(
    val id: Long,
    val title: String,
    val snippet: String,
    val feedTitle: String,
    val unread: Boolean,
    val pubDate: String,
    val imageUrl: String?,
    val link: String?,
    val bookmarked: Boolean,
    val feedImageUrl: URL?,
    val primarySortTime: Instant,
    val rawPubDate: ZonedDateTime?,
) {
    val cursor: FeedItemCursor
        get() = object : FeedItemCursor {
            override val primarySortTime: Instant = this@FeedListItem.primarySortTime
            override val pubDate: ZonedDateTime? = this@FeedListItem.rawPubDate
            override val id: Long = this@FeedListItem.id
        }

    /**
     * Used so lazylist/grid can re-use items.
     *
     * Type will depend on having images as that will influence visible items
     */
    fun contentType(feedItemStyle: FeedItemStyle): String = when {
        imageUrl?.isNotBlank() == true -> "$feedItemStyle/image"
        else -> "$feedItemStyle/other"
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewRead() {
    FeederTheme {
        Surface {
            FeedItemCompact(
                item = FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = false,
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
                bookmarkIndicator = true,
                maxLines = 5,
                imageWidth = 64.dp,
                showOnlyTitle = false,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewUnread() {
    FeederTheme {
        Surface {
            FeedItemCompact(
                item = FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
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
                bookmarkIndicator = true,
                maxLines = 5,
                imageWidth = 64.dp,
                showOnlyTitle = false,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun PreviewWithImage() {
    FeederTheme {
        Surface {
            Box(
                modifier = Modifier.width(400.dp),
            ) {
                FeedItemCompact(
                    item = FeedListItem(
                        title = "title",
                        snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                        feedTitle = "Super Duper Feed One two three hup di too dasf",
                        pubDate = "Jun 9, 2021",
                        unread = true,
                        imageUrl = "blabla",
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
                    bookmarkIndicator = true,
                    maxLines = 5,
                    imageWidth = 64.dp,
                    showOnlyTitle = false,
                )
            }
        }
    }
}
