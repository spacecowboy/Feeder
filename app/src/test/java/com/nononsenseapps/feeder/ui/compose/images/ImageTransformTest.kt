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
        assertEquals(ImageTransform(), ImageTransform().transformed(1f, Offset(500f, -500f), Offset.Zero, viewport, landscape))
    }

    @Test
    fun zoomIsBoundedAndResetRemovesPan() {
        val zoomed = ImageTransform().transformed(100f, Offset(500f, -500f), Offset(50f, 50f), viewport, landscape)
        assertEquals(ImageTransform(8f, Offset(350f, -150f)), zoomed)
        assertEquals(ImageTransform(), zoomed.transformed(0.001f, Offset.Zero, Offset.Zero, viewport, landscape))
    }

    @Test
    fun zoomKeepsTappedPointUnderFinger() {
        val result = ImageTransform().transformed(3f, Offset.Zero, Offset(75f, 50f), viewport, landscape)
        assertEquals(ImageTransform(3f, Offset(-50f, 0f)), result)
    }

    @Test
    fun panningUsesImageBoundsWithoutLetterboxing() {
        assertEquals(ImageTransform(2f, Offset(50f, 0f)), ImageTransform().transformed(2f, Offset(80f, 80f), Offset(50f, 50f), viewport, landscape))
    }

    @Test
    fun resizeClampsExistingPan() {
        val result = ImageTransform(3f, Offset(100f, 100f)).transformed(1f, Offset.Zero, Offset.Zero, Size(200f, 100f), landscape)
        assertEquals(ImageTransform(3f, Offset(100f, 100f)), result)
    }

    @Test
    fun unloadedImageDoesNotAccumulateGestures() {
        assertEquals(ImageTransform(), ImageTransform().transformed(3f, Offset(20f, 20f), Offset.Zero, viewport, Size.Zero))
    }
}
