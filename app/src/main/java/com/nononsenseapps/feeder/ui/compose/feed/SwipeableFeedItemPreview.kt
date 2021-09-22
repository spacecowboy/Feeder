package com.nononsenseapps.feeder.ui.compose.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.FeedItemStyle
import com.nononsenseapps.feeder.ui.compose.theme.SwipingItemToReadColor
import com.nononsenseapps.feeder.ui.compose.theme.SwipingItemToUnreadColor
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun SwipeableFeedItemPreview(
    onSwipe: suspend () -> Unit,
    onlyUnread: Boolean,
    item: FeedListItem,
    showThumbnail: Boolean,
    feedItemStyle: FeedItemStyle,
    imagePainter: @Composable (String) -> Unit,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onItemClick: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val isRtl = LocalLayoutDirection.current==LayoutDirection.Rtl
    val animatedVisibilityState = remember { MutableTransitionState(true) }
    val swipeableState = rememberSwipeableState(initialValue = FeedItemSwipeState.NONE)

    val color by animateColorAsState(
        when {
            swipeableState.targetValue==FeedItemSwipeState.NONE -> Color.Transparent
            item.unread -> SwipingItemToReadColor
            else -> SwipingItemToUnreadColor
        }
    )

    LaunchedEffect(key1 = onlyUnread, key2 = item.unread) {
        // critical state changes - reset ui state
        animatedVisibilityState.targetState = true
        swipeableState.animateTo(FeedItemSwipeState.NONE)
    }

    // Needs to be set once layout is complete
    var itemSize by remember { mutableStateOf(Size(1f, 1f)) }

    val anchors = mapOf(
        0f to FeedItemSwipeState.NONE,
        -itemSize.width to FeedItemSwipeState.LEFT,
        itemSize.width to FeedItemSwipeState.RIGHT
    )

    if (swipeableState.currentValue!=FeedItemSwipeState.NONE) {
        LaunchedEffect(swipeableState.currentValue) {
            if (onlyUnread) {
                animatedVisibilityState.targetState = false
            } else {
                onSwipe()
            }
        }
    }

    if (
        onlyUnread
        && animatedVisibilityState.isIdle
        && animatedVisibilityState.currentState!=item.unread
        && swipeableState.currentValue!=FeedItemSwipeState.NONE
    ) {
        LaunchedEffect(key1 = animatedVisibilityState.currentState, key2 = item.unread) {
            if (animatedVisibilityState.currentState!=item.unread) {
                // Reset swipe state here to avoid a race later since item will not receive update
                // when removed from list
                swipeableState.snapTo(FeedItemSwipeState.NONE)
                onSwipe()
            }
        }
    }

    var swipeIconAlignment by remember { mutableStateOf(Alignment.CenterStart) }
    // Launched effect because I don't want a value change to zero to change the variable
    LaunchedEffect(swipeableState.direction) {
        if (swipeableState.direction==1f) {
            swipeIconAlignment = Alignment.CenterStart
        } else if (swipeableState.direction==-1f) {
            swipeIconAlignment = Alignment.CenterEnd
        }
    }

    var dropDownMenuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    val markAboveAsReadLabel = stringResource(R.string.mark_items_above_as_read)
    val markBelowAsReadLabel = stringResource(R.string.mark_items_below_as_read)

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

    AnimatedVisibility(
        visibleState = animatedVisibilityState,
        enter = fadeIn(1f),
        exit = shrinkVertically(Alignment.CenterVertically) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { layoutCoordinates ->
                    itemSize = layoutCoordinates.size.toSize()
                }
                .combinedClickable(
                    onLongClick = {
                        dropDownMenuExpanded = true
                    },
                    onClick = onItemClick,
                )
                .semantics {
                    stateDescription = readStatusLabel
                    customActions = listOf(
                        CustomAccessibilityAction("toggle read status") {
                            coroutineScope.launch {
                                onSwipe()
                            }
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
                    )
                }
        ) {
            Box(
                contentAlignment = swipeIconAlignment,
                modifier = Modifier
                    .matchParentSize()
                    .background(color)
                    .padding(horizontal = 24.dp)
            ) {
                AnimatedVisibility(
                    visible = swipeableState.targetValue!=FeedItemSwipeState.NONE,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        when (item.unread) {
                            true -> Icons.Default.VisibilityOff
                            false -> Icons.Default.Visibility
                        },
                        contentDescription = null,
                    )
                }
            }

            when (feedItemStyle) {
                FeedItemStyle.CARD -> {
                    FeedItemCard(
                        item = item,
                        showThumbnail = showThumbnail,
                        imagePainter = imagePainter,
                        onMarkAboveAsRead = onMarkAboveAsRead,
                        onMarkBelowAsRead = onMarkBelowAsRead,
                        dropDownMenuExpanded = dropDownMenuExpanded,
                        onDismissDropdown = { dropDownMenuExpanded = false },
                        modifier = Modifier
                            .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                    )
                }
                FeedItemStyle.COMPACT -> {
                    FeedItemCompact(
                        item = item,
                        showThumbnail = showThumbnail,
                        imagePainter = imagePainter,
                        onMarkAboveAsRead = onMarkAboveAsRead,
                        onMarkBelowAsRead = onMarkBelowAsRead,
                        dropDownMenuExpanded = dropDownMenuExpanded,
                        onDismissDropdown = { dropDownMenuExpanded = false },
                        modifier = Modifier
                            .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                    )
                }
                FeedItemStyle.SUPER_COMPACT -> {
                    FeedItemSuperCompact(
                        item = item,
                        showThumbnail = showThumbnail,
                        imagePainter = imagePainter,
                        onMarkAboveAsRead = onMarkAboveAsRead,
                        onMarkBelowAsRead = onMarkBelowAsRead,
                        dropDownMenuExpanded = dropDownMenuExpanded,
                        onDismissDropdown = { dropDownMenuExpanded = false },
                        modifier = Modifier
                            .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                    )
                }
            }


            // This box handles swiping - it uses padding to allow the nav drawer to still be dragged
            // It's very important than clickable stuff is handled by its parent - or a direct child
            Box(
                modifier = Modifier
                    .padding(start = 48.dp)
                    .matchParentSize()
                    .swipeable(
                        state = swipeableState,
                        anchors = anchors,
                        orientation = Orientation.Horizontal,
                        reverseDirection = isRtl,
                        thresholds = { _, _ ->
                            FractionalThreshold(0.25f)
                        }
                    )
            )


        }
    }
}

enum class FeedItemSwipeState {
    NONE,
    LEFT,
    RIGHT
}
