package com.nononsenseapps.feeder.ui.compose.modifiers

/**
 * Courtesy of Paul Franco
 * https://dev.to/paulfranco/how-to-track-composable-visibility-in-jetpack-compose-with-a-custom-trackvisibility-modifier-5bad
 */

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
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
    private val minimumVisibilityDelta = 0.01f

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val boundsInWindow = coordinates.boundsInWindow()
        val parentBounds = coordinates.parentLayoutCoordinates?.boundsInWindow()

        if (parentBounds == null || !coordinates.isAttached) {
            previousVisibilityPercentage = 0f
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

        val visibilityDifference =
            previousVisibilityPercentage?.let { previous ->
                abs(visibilityPercentage - previous)
            } ?: Float.MAX_VALUE

        if (visibilityDifference >= minimumVisibilityDelta) {
            onVisibilityChanged(
                VisibilityInfo(
                    isVisible = visibilityPercentage > 0f,
                    visiblePercentage = visibilityPercentage,
                    bounds = boundsInWindow,
                    isAboveThreshold = visibilityPercentage >= thresholdPercentage,
                ),
            )
            previousVisibilityPercentage = visibilityPercentage
        }
    }
}

data class VisibilityInfo(
    val isVisible: Boolean,
    val visiblePercentage: Float,
    val bounds: Rect,
    val isAboveThreshold: Boolean,
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
