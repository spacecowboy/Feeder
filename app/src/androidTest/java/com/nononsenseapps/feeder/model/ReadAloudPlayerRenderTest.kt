package com.nononsenseapps.feeder.model

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.model.html.LinearArticle
import com.nononsenseapps.feeder.model.html.LinearText
import com.nononsenseapps.feeder.model.html.LinearTextBlockStyle
import com.nononsenseapps.feeder.ui.MainActivity
import com.nononsenseapps.feeder.ui.compose.html.ColumnArticleContent
import com.nononsenseapps.feeder.ui.compose.readaloud.SpeechHighlight
import com.nononsenseapps.feeder.ui.compose.readaloud.TTSPlayer
import com.nononsenseapps.feeder.ui.compose.theme.PreviewTheme
import com.nononsenseapps.feeder.ui.compose.utils.ImmutableHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ReadAloudPlayerRenderTest {
    @Test fun renderedSentenceHighlightAndTimeline() =
        runBlocking<Unit> {
            val app = getApplicationContext<FeederApplication>()
            val text = "This sentence has already been read. This sentence is being read aloud now. The final sentence comes next."
            val article = LinearArticle(listOf(LinearText(emptySet(), text, emptyList(), LinearTextBlockStyle.TEXT)))
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    activity.setContent {
                        PreviewTheme {
                            Surface(Modifier.fillMaxSize()) {
                                Column {
                                    Column(Modifier.weight(1f).padding(16.dp)) {
                                        ColumnArticleContent(article, { _, _ -> }, speechHighlight = SpeechHighlight(text, SpeechSegment(36, 74)))
                                    }
                                    TTSPlayer(true, {}, {}, {}, {}, ImmutableHolder(emptyList()), {}, progress = ReadAloudProgress(positionMillis = 125000, durationMillis = 306000))
                                }
                            }
                        }
                    }
                }
                delay(1000)
                scenario.onActivity { activity ->
                    val view = activity.window.decorView
                    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                    view.draw(Canvas(bitmap))
                    File(app.cacheDir, "read-aloud-preview.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    bitmap.recycle()
                }
            }
        }
}
