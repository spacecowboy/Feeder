package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.MediaImage
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import java.net.URL
import java.time.Instant

/**
 * Opacity for an entry that has been read.
 *
 * Lower than the 0.75 used before so the difference between read and unread is legible at a
 * glance in a dense list, where weight and colour alone are not enough of a signal. Shared by the
 * dense styles so they stay consistent with each other.
 */
internal const val READ_ITEM_ALPHA = 0.55f

@Composable
fun FeedItemSuperCompact(
    item: FeedListItem,
    onOpenFeedItemInReader: () -> Unit,
    onOpenFeedItemInCustomTab: () -> Unit,
    onOpenFeedItemInBrowser: () -> Unit,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onShareItem: () -> Unit,
    onToggleBookmark: () -> Unit,
    dropDownMenuExpanded: Boolean,
    onDismissDropdown: () -> Unit,
    bookmarkIndicator: Boolean,
    maxLines: Int,
    showOnlyTitle: Boolean,
    showReadingTime: Boolean,
    modifier: Modifier = Modifier,
) {
    // surfaceContainerLow is exactly what ElevatedCard uses for its container (Material3's
    // ElevatedCardTokens.ContainerColor), so a super compact row matches the compact card style's
    // tone using the same theme role rather than a color of its own. It is also what makes the
    // hairline gap between rows and the rounded corners read as shape.
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier =
            modifier
                .requiredHeightIn(min = minimumTouchSize)
                .alpha(if (!item.unread) READ_ITEM_ALPHA else 1.0f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = LocalDimens.current.margin),
        ) {
            FeedItemEitherIndicator(
                bookmarked = item.bookmarked && bookmarkIndicator,
                itemImage = null,
                feedImageUrl = item.feedImageUrl?.toString(),
                size = 24.dp,
            )
            FeedItemText(
                item = item,
                onOpenFeedItemInReader = onOpenFeedItemInReader,
                onOpenFeedItemInCustomTab = onOpenFeedItemInCustomTab,
                onOpenFeedItemInBrowser = onOpenFeedItemInBrowser,
                onMarkAboveAsRead = onMarkAboveAsRead,
                onMarkBelowAsRead = onMarkBelowAsRead,
                onShareItem = onShareItem,
                onToggleBookmark = onToggleBookmark,
                dropDownMenuExpanded = dropDownMenuExpanded,
                onDismissDropdown = onDismissDropdown,
                maxLines = maxLines,
                showOnlyTitle = showOnlyTitle,
                showReadingTime = showReadingTime,
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun PreviewRead() {
    PreviewTheme {
        FeedItemSuperCompact(
            item =
                @Suppress("ktlint:standard:max-line-length")
                FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = false,
                    image = null,
                    link = null,
                    id = ID_UNSET,
                    bookmarked = true,
                    feedImageUrl = null,
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                    wordCount = 900,
                ),
            onOpenFeedItemInReader = {},
            onOpenFeedItemInCustomTab = {},
            onOpenFeedItemInBrowser = {},
            onMarkAboveAsRead = {},
            onMarkBelowAsRead = {},
            onShareItem = {},
            onToggleBookmark = {},
            dropDownMenuExpanded = false,
            onDismissDropdown = {},
            bookmarkIndicator = true,
            maxLines = 2,
            showOnlyTitle = false,
            showReadingTime = true,
        )
    }
}

@Composable
@PreviewLightDark
private fun PreviewUnread() {
    PreviewTheme {
        FeedItemSuperCompact(
            item =
                @Suppress("ktlint:standard:max-line-length")
                FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = true,
                    image = null,
                    link = null,
                    id = ID_UNSET,
                    bookmarked = false,
                    feedImageUrl = null,
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                    wordCount = 900,
                ),
            onOpenFeedItemInReader = {},
            onOpenFeedItemInCustomTab = {},
            onOpenFeedItemInBrowser = {},
            onMarkAboveAsRead = {},
            onMarkBelowAsRead = {},
            onShareItem = {},
            onToggleBookmark = {},
            dropDownMenuExpanded = false,
            onDismissDropdown = {},
            bookmarkIndicator = true,
            maxLines = 2,
            showOnlyTitle = false,
            showReadingTime = true,
        )
    }
}

@Composable
@PreviewLightDark
private fun PreviewWithImage() {
    PreviewTheme {
        FeedItemSuperCompact(
            item =
                @Suppress("ktlint:standard:max-line-length")
                FeedListItem(
                    title = "title",
                    snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
                    feedTitle = "Super Duper Feed One two three hup di too dasf",
                    pubDate = "Jun 9, 2021",
                    unread = true,
                    image = MediaImage("blabla"),
                    link = null,
                    id = ID_UNSET,
                    bookmarked = false,
                    feedImageUrl = URL("https://example.com/image.png"),
                    primarySortTime = Instant.EPOCH,
                    rawPubDate = null,
                    wordCount = 900,
                ),
            onOpenFeedItemInReader = {},
            onOpenFeedItemInCustomTab = {},
            onOpenFeedItemInBrowser = {},
            onMarkAboveAsRead = {},
            onMarkBelowAsRead = {},
            onShareItem = {},
            onToggleBookmark = {},
            dropDownMenuExpanded = false,
            onDismissDropdown = {},
            bookmarkIndicator = true,
            maxLines = 2,
            showOnlyTitle = false,
            showReadingTime = true,
        )
    }
}
