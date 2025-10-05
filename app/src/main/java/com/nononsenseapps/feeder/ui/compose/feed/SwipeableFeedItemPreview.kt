package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
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
import com.nononsenseapps.feeder.util.logDebug
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val LOG_TAG = "FEEDER_SWIPEITEM"

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
    val coroutineScope = rememberCoroutineScope()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val anchoredDraggableState = remember { AnchoredDraggableState(FeedItemSwipeState.CENTER) }
    val onSwipeCallback by rememberUpdatedState(newValue = onSwipe)

    val color by animateColorAsState(
        targetValue =
            when {
                item.unread || filter.onlyUnread -> SwipingItemToReadColor
                else -> SwipingItemToUnreadColor
            },
        label = "swipeBackground",
    )

    LaunchedEffect(filter, item.unread) {
        // critical state changes - reset ui state
        if (anchoredDraggableState.currentValue != FeedItemSwipeState.CENTER) {
            anchoredDraggableState.animateTo(FeedItemSwipeState.CENTER)
        }
    }

    var skipHapticFeedback by remember { mutableStateOf(false) }
    LaunchedEffect(anchoredDraggableState.settledValue) {
        if (anchoredDraggableState.settledValue != FeedItemSwipeState.CENTER) {
            logDebug(LOG_TAG, "onSwipe ${item.unread}")
            if (!filter.onlyUnread) {
                skipHapticFeedback = true
                anchoredDraggableState.animateTo(FeedItemSwipeState.CENTER)
            }

            onSwipeCallback(item.unread)
        }
    }

    val swipeIconAlignment by remember {
        derivedStateOf {
            when {
                anchoredDraggableState.offset > 0 -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }
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

    BoxWithConstraints(
        modifier =
            modifier
                .width(dimens.maxContentWidth)
                .clip(
                    shape =
                        when (feedItemStyle) {
                            FeedItemStyle.COMPACT, FeedItemStyle.SUPER_COMPACT -> RectangleShape
                            else -> MaterialTheme.shapes.medium
                        },
                ).combinedClickable(
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
                                    onSwipe(item.unread)
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
    ) {
        val maxWidthPx =
            with(LocalDensity.current) {
                maxWidth.toPx()
            }

        val hapticFeedback = LocalHapticFeedback.current
        LaunchedEffect(anchoredDraggableState.currentValue) {
            try {
                if (anchoredDraggableState.requireOffset() == 0f) {
                    return@LaunchedEffect
                }
            } catch (_: IllegalStateException) {
                return@LaunchedEffect
            }

            if (skipHapticFeedback) {
                skipHapticFeedback = false
                return@LaunchedEffect
            }

            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        }

        val alpha by animateFloatAsState(
            targetValue = if (anchoredDraggableState.currentValue != FeedItemSwipeState.CENTER) 1.0f else 0.2f,
            animationSpec = tween(),
            label = "alphaAnimation",
        )

        Box(
            contentAlignment = swipeIconAlignment,
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
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

        val compactLandscape = isCompactLandscape()

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
                    modifier =
                        Modifier.offset {
                            try {
                                IntOffset(anchoredDraggableState.requireOffset().roundToInt(), 0)
                            } catch (_: IllegalStateException) {
                                IntOffset(0, 0)
                            }
                        },
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
                    modifier =
                        Modifier.offset {
                            try {
                                IntOffset(anchoredDraggableState.requireOffset().roundToInt(), 0)
                            } catch (_: IllegalStateException) {
                                IntOffset(0, 0)
                            }
                        },
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
                    modifier =
                        Modifier.offset {
                            try {
                                IntOffset(anchoredDraggableState.requireOffset().roundToInt(), 0)
                            } catch (_: IllegalStateException) {
                                IntOffset(0, 0)
                            }
                        },
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
                    modifier =
                        Modifier.offset {
                            try {
                                IntOffset(anchoredDraggableState.requireOffset().roundToInt(), 0)
                            } catch (_: IllegalStateException) {
                                IntOffset(0, 0)
                            }
                        },
                )
            }
        }

        // This box handles swiping - it uses padding to allow the nav drawer to still be dragged
        // It's very important that clickable stuff is handled by its parent - or a direct child
        // Wrapped in an outer box to get the height set properly
        if (swipeAsRead != SwipeAsRead.DISABLED) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .run {
                                @Suppress("KotlinConstantConditions")
                                when (swipeAsRead) {
                                    // This never actually gets called due to outer if
                                    SwipeAsRead.DISABLED ->
                                        this
                                            .height(0.dp)
                                            .width(0.dp)

                                    SwipeAsRead.ONLY_FROM_END -> {
                                        this
                                            .fillMaxHeight()
                                            .width(this@BoxWithConstraints.maxWidth / 4)
                                            .align(Alignment.CenterEnd)
                                    }

                                    SwipeAsRead.FROM_ANYWHERE -> {
                                        this
                                            .padding(start = 48.dp)
                                            .matchParentSize()
                                    }
                                }
                            }.anchoredDraggable(
                                state = anchoredDraggableState,
                                orientation = Orientation.Horizontal,
                                reverseDirection = isRtl,
                                enabled = swipeEnabled,
                            ),
                )

                // Dividing the maxWidth by 2 means you only have to swipe a quarter of the screen width to reach the swipe threshold, instead of a full half of the screen by default.
                LaunchedEffect(swipeAsRead) {
                    anchoredDraggableState.updateAnchors(
                        DraggableAnchors {
                            if (swipeAsRead == SwipeAsRead.ONLY_FROM_END) {
                                FeedItemSwipeState.START at -(maxWidthPx / 2)
                                FeedItemSwipeState.CENTER at 0f
                            } else if (swipeAsRead == SwipeAsRead.FROM_ANYWHERE) {
                                FeedItemSwipeState.START at -(maxWidthPx / 2)
                                FeedItemSwipeState.CENTER at 0f
                                FeedItemSwipeState.END at maxWidthPx / 2
                            }
                        },
                    )
                }
            }
        }
    }
}

enum class FeedItemSwipeState {
    CENTER,
    START,
    END,
}
