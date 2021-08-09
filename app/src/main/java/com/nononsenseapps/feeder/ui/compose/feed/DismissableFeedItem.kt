package com.nononsenseapps.feeder.ui.compose.feed

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun DismissableFeedItemPreview(
    onSwipe: suspend () -> Unit,
    onlyUnread: Boolean,
    item: FeedListItem,
    showThumbnail: Boolean,
    imagePainter: @Composable (String) -> Unit,
    onMarkAboveAsRead: () -> Unit,
    onMarkBelowAsRead: () -> Unit,
    onItemClick: () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmStateChange = {
            when (it) {
                DismissValue.Default -> false
                DismissValue.DismissedToEnd,
                DismissValue.DismissedToStart -> {
                    true
                }
            }
        }
    )

    AnimatedVisibility(
        visible = item.unread || !onlyUnread
    ) {
        SwipeToDismiss(
            state = dismissState,
            background = {
                val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                val color by animateColorAsState(
                    when {
                        dismissState.targetValue == DismissValue.Default -> Color.DarkGray
                        item.unread -> Color.Red
                        else -> Color.Green
                    }
                )
                val alignment = when (direction) {
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                }
                val icon = when (item.unread) {
                    true -> Icons.Default.VisibilityOff
                    false -> Icons.Default.Visibility
                }
                val scale by animateFloatAsState(
                    if (dismissState.targetValue == DismissValue.Default) {
                        0.75f
                    } else {
                        1f
                    }
                )

                Box(
                    contentAlignment = alignment,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = color)
                        .padding(horizontal = 20.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = "Toggle read status icon",
                        modifier = Modifier.scale(scale)
                    )
                }
            }
        ) {
            FeedItemPreview(
                item = item,
                onMarkAboveAsRead = onMarkAboveAsRead,
                onMarkBelowAsRead = onMarkBelowAsRead,
                showThumbnail = showThumbnail,
                imagePainter = imagePainter,
                onItemClick = onItemClick
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
val DismissState.isDismissed
    get() = isDismissed(DismissDirection.EndToStart) || isDismissed(DismissDirection.StartToEnd)
