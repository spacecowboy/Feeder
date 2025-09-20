package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.archmodel.SwipeAsRead
import com.nononsenseapps.feeder.ui.compose.components.safeSemantics
import com.nononsenseapps.feeder.ui.compose.feedarticle.FeedListFilter
import com.nononsenseapps.feeder.ui.compose.feedarticle.onlyUnread
import com.nononsenseapps.feeder.ui.compose.theme.LocalDimens
import com.nononsenseapps.feeder.ui.compose.theme.SwipingItemToReadColor
import com.nononsenseapps.feeder.ui.compose.theme.SwipingItemToUnreadColor
import com.nononsenseapps.feeder.ui.compose.utils.isCompactLandscape
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * OnSwipe takes a boolean parameter of the current read state of the item - so that it can be
 * called multiple times by several DisposableEffects.
 */
@Composable
fun SwipeableFeedItemPreview(
    onSwipe: (Boolean) -> Unit,
    filter: FeedListFilter,
    item: FeedListItem,
    showThumbnail: Boolean,
    feedItemStyle: FeedItemStyle,
    swipeAsRead: SwipeAsRead,
    bookmarkIndicator: Boolean,
    maxLines: Int,
    showOnlyTitle: Boolean,
    showReadingTime: Boolean,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShareItem: () -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
) {
    // Use rememberUpdatedState for these as they are referenced inside long-lived lambdas.
    val currentFilter by rememberUpdatedState(newValue = filter)
    val currentOnSwipeCallback by rememberUpdatedState(newValue = onSwipe)
    val currentItem by rememberUpdatedState(newValue = item)

    val swipeableState =
        rememberSwipeToDismissBoxState(confirmValueChange = {
            if (it != SwipeToDismissBoxValue.Settled) {
                currentOnSwipeCallback(currentItem.unread)

                // Only commit the change if we're only viewing unread items, in all other cases we'll snap back the swipe position as the item isn't removed from view.
                currentFilter.onlyUnread
            } else {
                false
            }
        })

    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    val threshold = SwipeToDismissBoxDefaults.positionalThreshold(0f)
    var thresholdReached by remember { mutableStateOf(false) }
    LaunchedEffect(swipeableState) {
        snapshotFlow {
            try {
                with(density) {
                    val offset = swipeableState.requireOffset().absoluteValue
                    offset >= threshold
                }
            } catch (_: IllegalStateException) {
                false
            }
        }.distinctUntilChanged()
            .collect { isOverThreshold ->
                if (isOverThreshold && !thresholdReached) {
                    thresholdReached = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                } else if (!isOverThreshold && thresholdReached) {
                    thresholdReached = false
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                }
            }
    }

    val color by animateColorAsState(
        targetValue =
            when {
                item.unread || filter.onlyUnread -> SwipingItemToReadColor
                else -> SwipingItemToUnreadColor
            },
        label = "swipeBackground",
    )

    val alpha by animateFloatAsState(
        targetValue = if (thresholdReached) 1.0f else 0.2f,
        animationSpec = tween(),
        label = "alphaAnimation",
    )

    LaunchedEffect(filter, item.unread) {
        // critical state changes - reset ui state
        if (swipeableState.currentValue != SwipeToDismissBoxValue.Settled) {
            swipeableState.reset()
        }
    }

    var swipeIconAlignment by remember { mutableStateOf(Alignment.CenterStart) }
    // Launched effect because I don't want a value change to zero to change the variable
    LaunchedEffect(swipeableState.dismissDirection) {
        if (swipeableState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
            swipeIconAlignment = Alignment.CenterStart
        } else if (swipeableState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
            swipeIconAlignment = Alignment.CenterEnd
        }
    }

    var dropDownMenuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    val toggleReadStatusLabel = stringResource(R.string.toggle_read_status)
    val saveArticleLabel = stringResource(R.string.save_article)
    val unSaveArticleLabel = stringResource(R.string.unsave_article)
    val markAboveAsReadLabel = stringResource(R.string.mark_items_above_as_read)
    val markBelowAsReadLabel = stringResource(R.string.mark_items_below_as_read)
    val shareLabel = stringResource(R.string.share)

    val unreadLabel = stringResource(R.string.unread_adjective)
    val alreadyReadLabel = stringResource(R.string.read_adjective)
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
    val compactLandscape = isCompactLandscape()
    val coroutineScope = rememberCoroutineScope()
    SwipeToDismissBox(
        state = swipeableState,
        backgroundContent = {
            Box(
                contentAlignment = swipeIconAlignment,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(
                            shape =
                                when (feedItemStyle) {
                                    FeedItemStyle.COMPACT, FeedItemStyle.SUPER_COMPACT -> RectangleShape
                                    else -> MaterialTheme.shapes.medium
                                },
                        ).graphicsLayer {
                            this.alpha = alpha
                        }.drawBehind {
                            drawRect(color = color)
                        }.padding(horizontal = 24.dp),
            ) {
                Icon(
                    when (item.unread) {
                        true -> Icons.Default.VisibilityOff
                        false -> Icons.Default.Visibility
                    },
                    contentDescription = null,
                )
            }
        },
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .combinedClickable(
                    onLongClick = {
                        dropDownMenuExpanded = true
                    },
                    onClick = onItemClick,
                ).safeSemantics {
                    stateDescription = readStatusLabel
                    customActions =
                        listOf(
                            CustomAccessibilityAction(toggleReadStatusLabel) {
                                coroutineScope.launch {
                                    currentOnSwipeCallback(item.unread)
                                }
                                true
                            },
                            CustomAccessibilityAction(
                                when (item.bookmarked) {
                                    true -> unSaveArticleLabel
                                    false -> saveArticleLabel
                                },
                            ) {
                                onToggleBookmark()
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
                },
        enableDismissFromStartToEnd = swipeAsRead == SwipeAsRead.FROM_ANYWHERE,
        enableDismissFromEndToStart = swipeAsRead == SwipeAsRead.FROM_ANYWHERE || swipeAsRead == SwipeAsRead.ONLY_FROM_END,
        gesturesEnabled = swipeAsRead != SwipeAsRead.DISABLED && swipeEnabled,
    ) {
        when (feedItemStyle) {
            FeedItemStyle.CARD -> {
                FeedItemCard(
                    item = item,
                    showThumbnail = showThumbnail && !compactLandscape,
                    onMarkAboveAsRead = onMarkAboveAsRead,
                    onMarkBelowAsRead = onMarkBelowAsRead,
                    onShareItem = onShareItem,
                    onToggleBookmark = onToggleBookmark,
                    dropDownMenuExpanded = dropDownMenuExpanded,
                    onDismissDropdown = { dropDownMenuExpanded = false },
                    bookmarkIndicator = bookmarkIndicator,
                    maxLines = maxLines,
                    showOnlyTitle = showOnlyTitle,
                    showReadingTime = showReadingTime,
                )
            }

            FeedItemStyle.COMPACT_CARD -> {
                FeedItemCompactCard(
                    state =
                        FeedItemState(
                            item = item,
                            showThumbnail = showThumbnail && !compactLandscape,
                            dropDownMenuExpanded = dropDownMenuExpanded,
                            bookmarkIndicator = bookmarkIndicator,
                            maxLines = maxLines,
                            showReadingTime = showReadingTime,
                        ),
                    onEvent = { event ->
                        when (event) {
                            FeedItemEvent.DismissDropdown -> {
                                dropDownMenuExpanded = false
                            }

                            FeedItemEvent.MarkAboveAsRead -> onMarkAboveAsRead()
                            FeedItemEvent.MarkBelowAsRead -> onMarkBelowAsRead()
                            FeedItemEvent.ShareItem -> onShareItem()
                            FeedItemEvent.ToggleBookmarked -> onToggleBookmark()
                        }
                    },
                )
            }

            FeedItemStyle.COMPACT -> {
                FeedItemCompact(
                    item = item,
                    showThumbnail = showThumbnail,
                    onMarkAboveAsRead = onMarkAboveAsRead,
                    onMarkBelowAsRead = onMarkBelowAsRead,
                    onShareItem = onShareItem,
                    onToggleBookmark = onToggleBookmark,
                    dropDownMenuExpanded = dropDownMenuExpanded,
                    onDismissDropdown = { dropDownMenuExpanded = false },
                    bookmarkIndicator = bookmarkIndicator,
                    maxLines = maxLines,
                    showOnlyTitle = showOnlyTitle,
                    showReadingTime = showReadingTime,
                    imageWidth =
                        when (compactLandscape) {
                            true -> 196.dp
                            false -> 64.dp
                        },
                )
            }

            FeedItemStyle.SUPER_COMPACT -> {
                FeedItemSuperCompact(
                    item = item,
                    onMarkAboveAsRead = onMarkAboveAsRead,
                    onMarkBelowAsRead = onMarkBelowAsRead,
                    onShareItem = onShareItem,
                    onToggleBookmark = onToggleBookmark,
                    dropDownMenuExpanded = dropDownMenuExpanded,
                    onDismissDropdown = { dropDownMenuExpanded = false },
                    bookmarkIndicator = bookmarkIndicator,
                    maxLines = maxLines,
                    showOnlyTitle = showOnlyTitle,
                    showReadingTime = showReadingTime,
                )
            }
        }
    }
}
