package com.nononsenseapps.feeder.ui.compose.images

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import com.nononsenseapps.feeder.model.html.LinearImage
import com.nononsenseapps.feeder.model.html.LinearImageSource
import com.nononsenseapps.feeder.ui.compose.html.LinearImageContent
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class ImageViewerTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun fixture(): String {
        val file = File(rule.activity.cacheDir, "viewer-test.png")
        val bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val paint =
            android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(20, 80, 160)
                textSize = 42f
            }
        for (i in 0..7) {
            canvas.drawLine(i * 100f, 0f, i * 100f, 800f, paint)
            canvas.drawLine(0f, i * 100f, 800f, i * 100f, paint)
            canvas.drawText("Detail ${i + 1}", 30f, i * 100f + 60f, paint)
        }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return file.toURI().toString()
    }

    @Test
    fun imageOpensZoomsResetsAndCloses() {
        val url = fixture()
        rule.setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize()) {
                    LinearImageContent(LinearImage(emptySet(), listOf(LinearImageSource(url, 800, 800, null, null)), null, null), emptyMap(), { _, _ -> })
                }
            }
        }
        rule.onNodeWithContentDescription("Zoom in").assertDoesNotExist()
        rule
            .onNode(
                androidx.compose.ui.test
                    .hasClickAction(),
            ).performClick()
        rule.onNodeWithContentDescription("Close image").assertIsDisplayed()
        rule.waitUntil(10_000) {
            runCatching {
                rule.onNodeWithContentDescription("Zoom in").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.Disabled]
                false
            }.getOrDefault(true)
        }
        rule.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            File(rule.activity.filesDir, "image-viewer-fit.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        rule.onNodeWithContentDescription("Zoom in").performClick()
        rule.onNode(hasStateDescription("200%")).assertExists()
        rule.onNode(hasStateDescription("200%")).performTouchInput { swipe(center, center + Offset(80f, 50f)) }
        rule.onNodeWithContentDescription("Reset zoom").performClick()
        rule.onNode(hasStateDescription("100%")).assertExists()
        rule.onNode(hasStateDescription("100%")).performTouchInput { doubleClick(center) }
        rule.onNode(hasStateDescription("300%")).assertExists()
        rule.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            File(rule.activity.filesDir, "image-viewer-zoom.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        rule.onNodeWithContentDescription("Reset zoom").performClick()
        rule.onNode(hasStateDescription("100%")).performTouchInput {
            pinch(center - Offset(40f, 0f), center + Offset(40f, 0f), center - Offset(160f, 0f), center + Offset(160f, 0f))
        }
        rule.onNode(hasStateDescription("100%")).assertDoesNotExist()
        rule.onNodeWithContentDescription("Close image").performClick()
        rule.onNodeWithContentDescription("Close image").assertDoesNotExist()
    }

    @Test
    fun linkedImageKeepsFragmentCallbackAndOffersViewer() {
        val url = fixture()
        var opened: Pair<String, Int?>? = null
        rule.setContent {
            MaterialTheme {
                LinearImageContent(LinearImage(emptySet(), listOf(LinearImageSource(url, 800, 800, null, null)), null, "https://example.com/#note"), mapOf("note" to 7), { link, index ->
                    opened =
                        link to index
                })
            }
        }
        rule
            .onNode(
                androidx.compose.ui.test
                    .hasClickAction(),
            ).performClick()
        rule.onNodeWithText("View image").assertIsDisplayed()
        rule.onNodeWithText("Open link").performClick()
        rule.runOnIdle { assertEquals("https://example.com/#note" to 7, opened) }
        rule
            .onNode(
                androidx.compose.ui.test
                    .hasClickAction(),
            ).performClick()
        rule.onNodeWithText("View image").performClick()
        rule.onNodeWithContentDescription("Close image").assertIsDisplayed()
    }

    @Test
    fun brokenImageOffersRetryAndDismissal() {
        rule.setContent { MaterialTheme { ImageViewer("file:///does-not-exist.png", null, {}) } }
        rule.waitUntil(10_000) {
            rule
                .onAllNodes(
                    androidx.compose.ui.test
                        .hasText("Retry"),
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
        rule.onNodeWithText("Retry").performClick()
        rule.onNodeWithContentDescription("Close image").assertIsDisplayed()
    }
}
