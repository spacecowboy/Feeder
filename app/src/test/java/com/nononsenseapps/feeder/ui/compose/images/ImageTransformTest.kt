package com.nononsenseapps.feeder.ui.compose.images

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageTransformTest {
    private val viewport = Size(100f, 100f)
    private val landscape = Size(200f, 100f)

    @Test
    fun fitImageCannotBePannedOutOfView() {
        val result =
            ImageTransform().transformed(
                zoom = 1f,
                pan = Offset(500f, -500f),
                centroid = Offset.Zero,
                viewport = viewport,
                image = landscape,
            )

        assertEquals(ImageTransform(), result)
    }

    @Test
    fun zoomIsBoundedAndResetRemovesPan() {
        val zoomed =
            ImageTransform().transformed(
                zoom = 100f,
                pan = Offset(500f, -500f),
                centroid = Offset(50f, 50f),
                viewport = viewport,
                image = landscape,
            )

        assertEquals(ImageTransform(8f, Offset(350f, -150f)), zoomed)
        assertEquals(
            ImageTransform(),
            zoomed.transformed(
                zoom = 0.001f,
                pan = Offset.Zero,
                centroid = Offset.Zero,
                viewport = viewport,
                image = landscape,
            ),
        )
    }

    @Test
    fun zoomKeepsTappedPointUnderFinger() {
        val result =
            ImageTransform().transformed(
                zoom = 3f,
                pan = Offset.Zero,
                centroid = Offset(75f, 50f),
                viewport = viewport,
                image = landscape,
            )

        assertEquals(ImageTransform(3f, Offset(-50f, 0f)), result)
    }

    @Test
    fun panningUsesImageBoundsWithoutLetterboxing() {
        val result =
            ImageTransform().transformed(
                zoom = 2f,
                pan = Offset(80f, 80f),
                centroid = Offset(50f, 50f),
                viewport = viewport,
                image = landscape,
            )

        assertEquals(ImageTransform(2f, Offset(50f, 0f)), result)
    }

    @Test
    fun resizeClampsExistingPan() {
        val result =
            ImageTransform(3f, Offset(100f, 100f)).transformed(
                zoom = 1f,
                pan = Offset.Zero,
                centroid = Offset.Zero,
                viewport = Size(200f, 100f),
                image = landscape,
            )

        assertEquals(ImageTransform(3f, Offset(100f, 100f)), result)
    }

    @Test
    fun unloadedImageDoesNotAccumulateGestures() {
        val result =
            ImageTransform().transformed(
                zoom = 3f,
                pan = Offset(20f, 20f),
                centroid = Offset.Zero,
                viewport = viewport,
                image = Size.Zero,
            )

        assertEquals(ImageTransform(), result)
    }
}
