package com.nononsenseapps.feeder.ui.compose.modifiers

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.nononsenseapps.feeder.ui.compose.minimumTouchSize
import com.nononsenseapps.feeder.util.logDebug

/**
 * Based on SO answer: https://stackoverflow.com/a/68056586/535073
 */
@Composable
fun Modifier.lazyListScrollbar(
    state: LazyListState,
    width: Dp = 8.dp,
): Modifier {
    // TODO 0 alpha
    val targetAlpha = if (state.isScrollInProgress) 1f else 0.1f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
    )

    val minimumSize = with(LocalDensity.current) {
        minimumTouchSize.toPx()
    }

    return drawWithContent {
        drawContent()

        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar) { //  && firstVisibleElementIndex != null) {
            // Injecting some spacer items hence the minus 2 and minus 1 so on
            val totalItems = state.layoutInfo.totalItemsCount.minus(2).coerceAtLeast(1)

            val firstVisibleElementIndex = (state.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0).toFloat()
            val lastVisibleElementIndex = (state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0).minus(1).coerceIn(0, totalItems - 1).toFloat()

            // 1 to 0
            // (totalItems - firstVisibleElementIndex) / totalItems
            val fw = (1 - firstVisibleElementIndex / totalItems)
            val lw = (lastVisibleElementIndex / totalItems)

            val weightedVisibleItemIndex = (firstVisibleElementIndex * fw + lastVisibleElementIndex * lw) / (lw + fw)

            val scrollFraction = weightedVisibleItemIndex / totalItems

            val scrollbarHeightPx =
                (this.size.height / totalItems).coerceAtLeast(minimumSize)
            logDebug("JONAS", "ScreenHeight: ${this.size.height}, bar: $scrollbarHeightPx, items: ${state.layoutInfo.totalItemsCount.coerceAtLeast(1)}, fv: $firstVisibleElementIndex, sf: $scrollFraction, wf: $weightedVisibleItemIndex, fw: $fw, lw: $lw")

//            val timesInHeight = this.size.height  / scrollbarHeightPx

//            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount

//            val scrollbarHeight = this.size.height / state.layoutInfo.totalItemsCount
//            val scrollbarOffsetY = firstVisibleElementIndex * scrollbarHeightPx

            val scrollbarOffsetY = scrollFraction * (this.size.height - scrollbarHeightPx)

            drawRect(
                color = Color.Red,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeightPx),
                alpha = alpha,
            )
        }
    }
}
