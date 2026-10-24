package com.nononsenseapps.feeder.ui.compose.images

import com.nononsenseapps.feeder.model.html.LinearImage
import com.nononsenseapps.feeder.model.html.LinearImageSource
import com.nononsenseapps.feeder.ui.compose.html.getViewerImageSource
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageSourceTest {
    private fun source(
        url: String,
        width: Int? = null,
        density: Float? = null,
    ) = LinearImageSource(url, width, null, density, null)

    private fun image(vararg sources: LinearImageSource) = LinearImage(emptySet(), sources.toList(), null, null)

    @Test
    fun declaredResolutionWinsOverUnknownFallback() {
        assertEquals("large", image(source("fallback"), source("small", 400), source("large", 2400)).getViewerImageSource()?.imgUri)
    }

    @Test
    fun densitySourcesUseHighestResolution() {
        assertEquals("3x", image(source("1x", density = 1f), source("3x", density = 3f)).getViewerImageSource()?.imgUri)
    }

    @Test
    fun emptySourcesAreSafe() {
        assertEquals(null, image().getViewerImageSource())
    }
}
