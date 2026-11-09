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
        val image =
            image(
                source("fallback"),
                source("small", 400),
                source("large", 2400),
            )

        assertEquals("large", image.getViewerImageSource()?.imgUri)
    }

    @Test
    fun densitySourcesUseHighestResolution() {
        val image =
            image(
                source("1x", density = 1f),
                source("3x", density = 3f),
            )

        assertEquals("3x", image.getViewerImageSource()?.imgUri)
    }

    @Test
    fun emptySourcesAreSafe() {
        assertEquals(null, image().getViewerImageSource())
    }
}
