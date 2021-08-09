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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemDateStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemFeedTitleStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemStyle
import com.nononsenseapps.feeder.ui.compose.theme.FeedListItemTitleStyle
import com.nononsenseapps.feeder.ui.compose.theme.SwipingItemToReadColor
import com.nononsenseapps.feeder.ui.compose.theme.SwipingItemToUnreadColor
import com.nononsenseapps.feeder.ui.compose.theme.keyline1Padding
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*
import kotlin.math.roundToInt

private val shortDateTimeFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())

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
    imagePainter: @Composable (String) -> Unit,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onItemClick: () -> Unit
) {
    val animatedVisibilityState = remember { MutableTransitionState(true) }
    val swipeableState = rememberSwipeableState(initialValue = FeedItemSwipeState.NONE)

    val color by animateColorAsState(
        when {
            swipeableState.targetValue == FeedItemSwipeState.NONE -> Color.Transparent
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
    var itemSize by remember { mutableStateOf(Size(9999f, 9999f)) }

    val anchors = mapOf(
        0f to FeedItemSwipeState.NONE,
        -itemSize.width to FeedItemSwipeState.LEFT,
        itemSize.width to FeedItemSwipeState.RIGHT
    )

    if (swipeableState.currentValue != FeedItemSwipeState.NONE) {
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
        && animatedVisibilityState.currentState != item.unread
        && swipeableState.currentValue != FeedItemSwipeState.NONE
    ) {
        LaunchedEffect(key1 = animatedVisibilityState.currentState, key2 = item.unread) {
            if (animatedVisibilityState.currentState != item.unread) {
                // Reset swipe state here to avoid a race later since item will not receive update
                // when removed from list
                swipeableState.snapTo(FeedItemSwipeState.NONE)
                onSwipe()
            }
        }
    }

    var swipeIconAlignment by remember { mutableStateOf(Alignment.CenterStart) }
    // Launched effect because I don't want a value change to zero to change it variable
    LaunchedEffect(swipeableState.direction) {
        if (swipeableState.direction == 1f) {
            swipeIconAlignment = Alignment.CenterStart
        } else if (swipeableState.direction == -1f) {
            swipeIconAlignment = Alignment.CenterEnd
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
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    orientation = Orientation.Horizontal,
                    thresholds = { from, to ->
                        FractionalThreshold(0.25f)
                    }
                )
        ) {
            Box(
                contentAlignment = swipeIconAlignment,
                modifier = Modifier
                    .matchParentSize()
                    .background(color)
                    .padding(horizontal = 24.dp)
            ) {
                AnimatedVisibility(
                    visible = swipeableState.targetValue != FeedItemSwipeState.NONE,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        when (item.unread) {
                            true -> Icons.Default.VisibilityOff
                            false -> Icons.Default.Visibility
                        },
                        contentDescription = stringResource(id = R.string.toggle_read_status)
                    )
                }
            }

            FeedItemPreview(
                item = item,
                showThumbnail = showThumbnail,
                imagePainter = imagePainter,
                onMarkAboveAsRead = onMarkAboveAsRead,
                onMarkBelowAsRead = onMarkBelowAsRead,
                onItemClick = onItemClick,
                modifier = Modifier
                    .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
            )
        }
    }
}

enum class FeedItemSwipeState {
    NONE,
    LEFT,
    RIGHT
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedItemPreview(
    item: FeedListItem,
    showThumbnail: Boolean,
    imagePainter: @Composable (String) -> Unit,
    modifier: Modifier = Modifier,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onItemClick: () -> Unit
) {
    var dropDownMenuExpanded by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = modifier
            .combinedClickable(
                onLongClick = {
                    dropDownMenuExpanded = true
                },
                onClick = onItemClick
            )
            .padding(
                start = keyline1Padding,
                end = if (item.imageUrl?.isNotBlank() != true || !showThumbnail) keyline1Padding else 0.dp
            )
            .height(IntrinsicSize.Min)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(weight = 1.0f, fill = true)
                .requiredHeightIn(min = minimumTouchSize)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = item.feedTitle,
                        style = FeedListItemFeedTitleStyle(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(weight = 1.0f, fill = true)
                            .padding(top = 2.dp)
                    )
                    Text(
                        text = item.pubDate?.toLocalDate()?.format(shortDateTimeFormat)
                            ?: "",
                        style = FeedListItemDateStyle(),
                        maxLines = 1,
                        modifier = Modifier
                            .padding(top = 2.dp, start = 4.dp)
                    )
                }
            }

            val alpha = if (item.unread) {
                ContentAlpha.high
            } else {
                ContentAlpha.medium
            }
            CompositionLocalProvider(LocalContentAlpha provides alpha) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            FeedListItemTitleStyle()
                        ) {
                            append(item.title)
                        }
                        append(" — ${item.snippet}…")
                    },
                    style = FeedListItemStyle(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 5,
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                )
            }

        }

        if (showThumbnail) {
            item.imageUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.width(4.dp))

                imagePainter(imageUrl)
            }
        }

        DropdownMenu(
            expanded = dropDownMenuExpanded,
            onDismissRequest = { dropDownMenuExpanded = !dropDownMenuExpanded }
        ) {
            DropdownMenuItem(
                onClick = {
                    dropDownMenuExpanded = false
                    onMarkAboveAsRead()
                }
            ) {
                Text(
                    text = stringResource(id = R.string.mark_items_above_as_read)
                )
            }
            DropdownMenuItem(
                onClick = {
                    dropDownMenuExpanded = false
                    onMarkBelowAsRead()
                }
            ) {
                Text(
                    text = stringResource(id = R.string.mark_items_below_as_read)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun preview() {
    FeedItemPreview(
        item = FeedListItem(
            title = "title",
            snippet = "snippet which is quite long as you might expect from a snipper of a story. It keeps going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and going and snowing",
            feedTitle = "Super Duper Feed One two three hup di too dasf",
            pubDate = null,
            unread = true,
            imageUrl = null,
            id = ID_UNSET
        ),
        showThumbnail = true,
        imagePainter = {},
        onMarkAboveAsRead = {},
        onMarkBelowAsRead = {}
    ) {

    }
}

@Immutable
data class FeedListItem(
    val id: Long,
    val title: String,
    val snippet: String,
    val feedTitle: String,
    val unread: Boolean,
    val pubDate: ZonedDateTime?,
    val imageUrl: String?,
)
