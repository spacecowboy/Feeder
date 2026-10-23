package com.nononsenseapps.feeder.model

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.archmodel.ThemeOptions
import com.nononsenseapps.feeder.model.html.LinearArticle
import com.nononsenseapps.feeder.model.html.LinearText
import com.nononsenseapps.feeder.model.html.LinearTextAnnotation
import com.nononsenseapps.feeder.model.html.LinearTextAnnotationLink
import com.nononsenseapps.feeder.model.html.LinearTextBlockStyle
import com.nononsenseapps.feeder.ui.MainActivity
import com.nononsenseapps.feeder.ui.compose.html.ColumnArticleContent
import com.nononsenseapps.feeder.ui.compose.readaloud.SpeechFollowViewport
import com.nononsenseapps.feeder.ui.compose.readaloud.SpeechHighlight
import com.nononsenseapps.feeder.ui.compose.readaloud.TTSPlayer
import com.nononsenseapps.feeder.ui.compose.readaloud.stopSpeechFollowingOnAccessibilityScroll
import com.nononsenseapps.feeder.ui.compose.readaloud.stopSpeechFollowingOnUserScroll
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ReadAloudPlayerRenderTest {
    @get:Rule val composeRule = createEmptyComposeRule()

    @get:Rule val keepScreenOnRule = KeepScreenOnRule()

    @Test fun changingDurationEstimateDidNotDiscardAnActiveScrub() {
        val app = getApplicationContext<FeederApplication>()
        val progress = mutableStateOf(ReadAloudProgress(articleId = -42, positionMillis = 1000, durationMillis = 100000, generatedMillis = 90000, isDurationEstimated = true))
        var sought = 0L
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    PreviewTheme {
                        TTSPlayer(true, {}, {}, {}, {}, ImmutableHolder(emptyList()), {}, progress = progress.value, onSeekTo = { sought = it })
                    }
                }
            }
            composeRule.waitForIdle()
            val slider = composeRule.onNodeWithContentDescription(app.getString(R.string.read_aloud_position))
            slider.performTouchInput {
                down(center)
                moveTo(center + Offset(30f, 0f))
            }
            scenario.onActivity { progress.value = progress.value.copy(durationMillis = 120000, generatedMillis = 100000) }
            composeRule.waitForIdle()
            slider.performTouchInput { up() }
            composeRule.waitForIdle()
            assertTrue("The active drag must still seek after an estimate update", sought > 30000)
        }
    }

    @Test fun followedSpeechUntilUserScrolledAndResumedWhenRequested() =
        runBlocking<Unit> {
            val spoken = "Spoken sentence."
            val next = "Next sentence."
            val paragraphs = (1..20).map { "Article paragraph number $it with some text to read." } + listOf(spoken, next) + (21..40).map { "Article paragraph number $it with some text to read." }
            val source = paragraphs.joinToString("\n\n")
            val start = source.indexOf("Spoken sentence")
            val segment = SpeechSegment(start, start + spoken.length)
            val article =
                LinearArticle(
                    paragraphs.map {
                        val annotations = if (it == spoken) listOf(LinearTextAnnotation(LinearTextAnnotationLink("https://example.com"), 0, it.lastIndex)) else emptyList()
                        LinearText(emptySet(), it, annotations, LinearTextBlockStyle.TEXT)
                    },
                )
            val highlight = mutableStateOf<SpeechHighlight?>(null)
            lateinit var scroll: ScrollState
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    activity.setContent {
                        scroll = rememberScrollState()
                        PreviewTheme {
                            SpeechFollowViewport(centerSpeech = highlight.value?.followSpokenText == true) {
                                Column(
                                    Modifier.fillMaxSize().stopSpeechFollowingOnUserScroll {
                                        highlight.value = highlight.value?.copy(followSpokenText = false)
                                    },
                                ) {
                                    Column(
                                        Modifier
                                            .weight(1f)
                                            .stopSpeechFollowingOnAccessibilityScroll(scroll) {
                                                highlight.value = highlight.value?.copy(followSpokenText = false)
                                            }.verticalScroll(scroll)
                                            .testTag("speech-reader"),
                                    ) {
                                        ColumnArticleContent(article, { _, _ -> }, speechHighlight = highlight.value)
                                    }
                                    TTSPlayer(
                                        true,
                                        {},
                                        {},
                                        {},
                                        {},
                                        ImmutableHolder(emptyList()),
                                        {},
                                        progress = ReadAloudProgress(articleId = -42, followSpokenText = highlight.value?.followSpokenText == true),
                                        displayedArticleId = -42,
                                        onGoToSpokenText = { highlight.value = highlight.value?.copy(focusRequestId = 2, followSpokenText = true) },
                                    )
                                }
                            }
                        }
                    }
                }
                composeRule.waitForIdle()
                scenario.onActivity { highlight.value = SpeechHighlight(source, segment, 1, followSpokenText = true) }
                composeRule.waitForIdle()
                assertTrue(withContext(Dispatchers.Main) { scroll.value > 0 })
                assertEquals(true, highlight.value?.followSpokenText)

                fun assertCentered(text: String) {
                    val reader = composeRule.onNodeWithTag("speech-reader").fetchSemanticsNode().boundsInRoot
                    val sentence = composeRule.onNodeWithText(text).fetchSemanticsNode().boundsInRoot
                    assertEquals(reader.center.y, sentence.center.y, 2f)
                }
                assertCentered(spoken)
                val app = getApplicationContext<FeederApplication>()
                composeRule.onNodeWithContentDescription(app.getString(R.string.go_to_spoken_text)).assertIsSelected()
                composeRule.onNodeWithTag("speech-reader").performTouchInput { swipeDown() }
                composeRule.waitForIdle()
                assertEquals(false, highlight.value?.followSpokenText)
                composeRule.onNodeWithContentDescription(app.getString(R.string.go_to_spoken_text)).assertIsNotSelected()
                withContext(Dispatchers.Main) { scroll.scrollTo(0) }
                val nextStart = source.indexOf(next)
                scenario.onActivity { highlight.value = SpeechHighlight(source, SpeechSegment(nextStart, nextStart + next.length), 1, followSpokenText = false) }
                composeRule.waitForIdle()
                assertEquals(0, withContext(Dispatchers.Main) { scroll.value })
                composeRule.onNodeWithContentDescription(app.getString(R.string.go_to_spoken_text)).performClick()
                composeRule.waitForIdle()
                assertTrue(withContext(Dispatchers.Main) { scroll.value > 0 })
                assertCentered(next)
                composeRule.onNodeWithContentDescription(app.getString(R.string.go_to_spoken_text)).assertIsSelected()
                composeRule.onNodeWithTag("speech-reader").performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -100f) }
                composeRule.waitForIdle()
                assertEquals(false, highlight.value?.followSpokenText)
            }
        }

    @Test fun renderedSentenceHighlightAndTimeline() =
        runBlocking<Unit> {
            val app = getApplicationContext<FeederApplication>()
            val text = "This sentence has already been read. This sentence is being read aloud now. The final sentence comes next."
            val article = LinearArticle(listOf(LinearText(emptySet(), text, emptyList(), LinearTextBlockStyle.TEXT)))
            for (theme in listOf(ThemeOptions.DAY, ThemeOptions.NIGHT)) {
                val progress =
                    mutableStateOf(ReadAloudProgress(articleId = -42, positionMillis = 125000, durationMillis = 351000, followSpokenText = true, isDurationEstimated = true, synthesisProgress = 0.38f))
                ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                    scenario.onActivity { activity ->
                        activity.setContent {
                            PreviewTheme(currentTheme = theme) {
                                Surface(Modifier.fillMaxSize()) {
                                    Column {
                                        Column(Modifier.weight(1f).padding(16.dp)) {
                                            ColumnArticleContent(article, { _, _ -> }, speechHighlight = SpeechHighlight(text, SpeechSegment(36, 74)))
                                        }
                                        TTSPlayer(
                                            true,
                                            {},
                                            {},
                                            {},
                                            {},
                                            ImmutableHolder(emptyList()),
                                            {},
                                            progress = progress.value,
                                            displayedArticleId = -42,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    composeRule.waitForIdle()
                    composeRule.onNodeWithText(app.getString(R.string.read_aloud_estimated_time, 38, "5:51")).assertExists()
                    delay(1000)
                    scenario.onActivity { activity ->
                        val view = activity.window.decorView
                        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                        view.draw(Canvas(bitmap))
                        File(app.cacheDir, "read-aloud-preview-${theme.name}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        bitmap.recycle()
                    }
                    scenario.onActivity { progress.value = progress.value.copy(isDurationEstimated = false, synthesisProgress = 1f) }
                    composeRule.waitForIdle()
                    composeRule.onNodeWithText("5:51").assertExists()
                    composeRule.onNodeWithText(app.getString(R.string.read_aloud_estimated_time, 38, "5:51")).assertDoesNotExist()
                }
            }
        }
}
