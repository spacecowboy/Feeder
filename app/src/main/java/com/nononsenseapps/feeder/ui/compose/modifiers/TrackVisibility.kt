package com.nononsenseapps.feeder.ui.compose.modifiers

/**
 * Courtesy of Paul Franco
 * https://dev.to/paulfranco/how-to-track-composable-visibility-in-jetpack-compose-with-a-custom-trackvisibility-modifier-5bad
 */

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun Modifier.trackVisibility(
    thresholdPercentage: Float = 0.5f,
    onVisibilityChanged: (VisibilityInfo) -> Unit,
): Modifier = this.then(VisibilityTrackerElement(thresholdPercentage, onVisibilityChanged))

private class VisibilityTrackerNode(
    var thresholdPercentage: Float,
    var onVisibilityChanged: (VisibilityInfo) -> Unit,
) : Modifier.Node(),
    GlobalPositionAwareModifierNode {
    private var previousVisibilityPercentage: Float? = null
    private var previousScrolledOffTop: Boolean? = null

    /**
     * Top edge of the visible area. Cached because the parent bounds are empty once the layout has
     * been clipped away completely.
     */
    private var lastKnownVisibleTop: Float? = null
    private val minimumVisibilityDelta = 0.01f

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val boundsInWindow = coordinates.boundsInWindow()
        val parentBounds = coordinates.parentLayoutCoordinates?.boundsInWindow()

        if (parentBounds == null || !coordinates.isAttached) {
            previousVisibilityPercentage = 0f
            previousScrolledOffTop = null
            return
        }

        val visibleLeft = max(boundsInWindow.left, parentBounds.left)
        val visibleRight = min(boundsInWindow.right, parentBounds.right)
        val visibleTop = max(boundsInWindow.top, parentBounds.top)
        val visibleBottom = min(boundsInWindow.bottom, parentBounds.bottom)

        val visibleWidth = max(0f, visibleRight - visibleLeft)
        val visibleHeight = max(0f, visibleBottom - visibleTop)

        val visibleArea = visibleWidth * visibleHeight
        val totalArea = (coordinates.size.width * coordinates.size.height).toFloat().takeIf { it > 0 } ?: return

        val visibilityPercentage = (visibleArea / totalArea).coerceIn(0f, 1f)

        if (!parentBounds.isEmpty) {
            lastKnownVisibleTop = parentBounds.top
        }

        // boundsInWindow is clipped, so the unclipped position is needed to tell whether the layout
        // left the visible area at the top (scrolled past) or at the bottom (scrolled back, or
        // pushed down by items inserted above it)
        val scrolledOffTop =
            lastKnownVisibleTop?.let { visibleTop ->
                coordinates.positionInWindow().y < visibleTop - TOP_EDGE_TOLERANCE_PX
            } ?: previousScrolledOffTop ?: false

        val visibilityDifference =
            previousVisibilityPercentage?.let { previous ->
                abs(visibilityPercentage - previous)
            } ?: Float.MAX_VALUE

        if (visibilityDifference >= minimumVisibilityDelta || scrolledOffTop != previousScrolledOffTop) {
            onVisibilityChanged(
                VisibilityInfo(
                    isVisible = visibilityPercentage > 0f,
                    visiblePercentage = visibilityPercentage,
                    bounds = boundsInWindow,
                    isAboveThreshold = visibilityPercentage >= thresholdPercentage,
                    isScrolledOffTop = scrolledOffTop,
                ),
            )
            previousVisibilityPercentage = visibilityPercentage
            previousScrolledOffTop = scrolledOffTop
        }
    }
}

/**
 * Sub-pixel differences are rounding noise, not scrolling.
 */
private const val TOP_EDGE_TOLERANCE_PX = 1f

data class VisibilityInfo(
    val isVisible: Boolean,
    val visiblePercentage: Float,
    val bounds: Rect,
    val isAboveThreshold: Boolean,
    /**
     * True when the top edge of the layout is above the top edge of its parent - in other words
     * when it is scrolling off the top of the screen. False while it is fully inside the parent,
     * and also when it leaves at the bottom.
     */
    val isScrolledOffTop: Boolean,
)

private class VisibilityTrackerElement(
    private val thresholdPercentage: Float,
    private val onVisibilityChanged: (VisibilityInfo) -> Unit,
) : ModifierNodeElement<VisibilityTrackerNode>() {
    override fun create() = VisibilityTrackerNode(thresholdPercentage, onVisibilityChanged)

    override fun update(node: VisibilityTrackerNode) {
        node.thresholdPercentage = thresholdPercentage
        node.onVisibilityChanged = onVisibilityChanged
    }

    override fun equals(other: Any?) =
        other is VisibilityTrackerElement &&
            other.thresholdPercentage == thresholdPercentage &&
            other.onVisibilityChanged == onVisibilityChanged

    override fun hashCode(): Int {
        var result = thresholdPercentage.hashCode()
        result = 31 * result + onVisibilityChanged.hashCode()
        return result
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "trackVisibility"
        properties["thresholdPercentage"] = thresholdPercentage
        properties["onVisibilityChanged"] = onVisibilityChanged
    }
}
