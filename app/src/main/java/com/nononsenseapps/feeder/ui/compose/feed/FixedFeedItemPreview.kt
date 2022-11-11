package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FixedFeedItemPreview(
    item: FeedListItem,
    showThumbnail: Boolean,
    feedItemStyle: FeedItemStyle,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleBookmarked: () -> Unit,
    onShareItem: () -> Unit,
    onItemClick: () -> Unit,
) {
    var dropDownMenuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    val pinArticleLabel = stringResource(R.string.pin_article)
    val unpinArticleLabel = stringResource(R.string.unpin_article)
    val bookmarkArticleLabel = stringResource(R.string.bookmark_article)
    val removeBookmarkLabel = stringResource(R.string.remove_bookmark)
    val markAboveAsReadLabel = stringResource(R.string.mark_items_above_as_read)
    val markBelowAsReadLabel = stringResource(R.string.mark_items_below_as_read)
    val shareLabel = stringResource(R.string.share)

    val unreadLabel = stringResource(R.string.unread)
    val alreadyReadLabel = stringResource(R.string.already_read)
    val readStatusLabel by remember(item.unread) {
        derivedStateOf {
            if (item.unread) {
                unreadLabel
            } else {
                alreadyReadLabel
            }
        }
    }

    val dimens = LocalDimens.current

    BoxWithConstraints(
        modifier = Modifier
            .width(dimens.maxContentWidth)
            .combinedClickable(
                onLongClick = {
                    dropDownMenuExpanded = true
                },
                onClick = onItemClick,
            )
            .semantics {
                try {
                    stateDescription = readStatusLabel
                    customActions = listOf(
                        CustomAccessibilityAction(
                            when (item.pinned) {
                                true -> unpinArticleLabel
                                false -> pinArticleLabel
                            }
                        ) {
                            onTogglePinned()
                            true
                        },
                        CustomAccessibilityAction(
                            when (item.bookmarked) {
                                true -> removeBookmarkLabel
                                false -> bookmarkArticleLabel
                            }
                        ) {
                            onToggleBookmarked()
                            true
                        },
                        CustomAccessibilityAction(markAboveAsReadLabel) {
                            onMarkAboveAsRead()
                            true
                        },
                        CustomAccessibilityAction(markBelowAsReadLabel) {
                            onMarkBelowAsRead()
                            true
                        },
                        CustomAccessibilityAction(shareLabel) {
                            onShareItem()
                            true
                        },
                    )
                } catch (e: Exception) {
                    // Observed nullpointer exception when setting customActions
                    // No clue why it could be null
                    Log.e("FeederSwipeableFIP", "Exception in semantics", e)
                }
            }
    ) {
        when (feedItemStyle) {
            FeedItemStyle.CARD -> {
                FeedItemCard(
                    item = item,
                    showThumbnail = showThumbnail,
                    onMarkAboveAsRead = onMarkAboveAsRead,
                    onMarkBelowAsRead = onMarkBelowAsRead,
                    onShareItem = onShareItem,
                    newIndicator = true,
                    dropDownMenuExpanded = dropDownMenuExpanded,
                    onDismissDropdown = { dropDownMenuExpanded = false },
                    onTogglePinned = onTogglePinned,
                    onToggleBookmarked = onToggleBookmarked,
                )
            }
            FeedItemStyle.COMPACT -> {
                FeedItemCompact(
                    item = item,
                    showThumbnail = showThumbnail,
                    onMarkAboveAsRead = onMarkAboveAsRead,
                    onMarkBelowAsRead = onMarkBelowAsRead,
                    onShareItem = onShareItem,
                    newIndicator = false,
                    dropDownMenuExpanded = dropDownMenuExpanded,
                    onDismissDropdown = { dropDownMenuExpanded = false },
                    onTogglePinned = onTogglePinned,
                    onToggleBookmarked = onToggleBookmarked,
                )
            }
            FeedItemStyle.SUPER_COMPACT -> {
                FeedItemSuperCompact(
                    item = item,
                    showThumbnail = showThumbnail,
                    onMarkAboveAsRead = onMarkAboveAsRead,
                    onMarkBelowAsRead = onMarkBelowAsRead,
                    onShareItem = onShareItem,
                    newIndicator = false,
                    dropDownMenuExpanded = dropDownMenuExpanded,
                    onDismissDropdown = { dropDownMenuExpanded = false },
                    onTogglePinned = onTogglePinned,
                    onToggleBookmarked = onToggleBookmarked,
                )
            }
        }
    }
}
