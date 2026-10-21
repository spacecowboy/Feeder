package com.nononsenseapps.feeder.model

import android.content.ComponentName
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.Lifecycle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nononsenseapps.feeder.FeederApplication
import com.nononsenseapps.feeder.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.direct
import org.kodein.di.instance
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ReadAloudPlaybackTest {
    @Test fun synthesizedAudioSupportedSystemPauseSeekAndResume() =
        runBlocking<Unit> {
            val app = getApplicationContext<FeederApplication>()
            val holder: TTSStateHolder = app.di.direct.instance()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                try {
                    withContext(Dispatchers.Main) {
                        holder.setLanguage(ForcedLocale(java.util.Locale.ENGLISH))
                        holder.tts(listOf(AnnotatedString("This is the first sentence. This is the second sentence. This is the last sentence.")), false, -42, "Read-aloud test")
                    }
                    val prepared = withTimeout(60000) { holder.progress.first { it.durationMillis > 0 || it.error != null } }
                    assertNull(prepared.error)
                    assertTrue(prepared.durationMillis > 1000)
                    val future =
                        withContext(Dispatchers.Main) {
                            MediaController.Builder(app, SessionToken(app, ComponentName(app, ReadAloudService::class.java))).buildAsync()
                        }
                    val controller = withContext(Dispatchers.IO) { future.get(10, TimeUnit.SECONDS) }
                    try {
                        withTimeout(10000) { while (withContext(Dispatchers.Main) { controller.duration } <= 0) delay(50) }
                        withContext(Dispatchers.Main) {
                            assertTrue(kotlin.math.abs(controller.duration - prepared.durationMillis) < 50)
                            assertEquals("Read-aloud test", controller.mediaMetadata.title.toString())
                        }
                        withContext(Dispatchers.Main) { controller.pause() }
                        withTimeout(5000) { holder.ttsState.first { it == PlaybackStatus.PAUSED } }
                        val target = prepared.durationMillis / 2
                        withContext(Dispatchers.Main) { controller.seekTo(target) }
                        withTimeout(5000) { holder.progress.first { kotlin.math.abs(it.positionMillis - target) < 250 && it.segment != null } }
                        withContext(Dispatchers.Main) { controller.play() }
                        withTimeout(5000) { holder.ttsState.first { it == PlaybackStatus.PLAYING } }
                        withTimeout(5000) { holder.progress.first { it.positionMillis > target + 300 } }
                        val beforeBackground = holder.progress.value.positionMillis
                        scenario.moveToState(Lifecycle.State.CREATED)
                        withTimeout(5000) { holder.progress.first { it.positionMillis > beforeBackground + 300 } }
                    } finally {
                        withContext(Dispatchers.Main) { controller.release() }
                    }
                } finally {
                    withContext(Dispatchers.Main) { holder.stop() }
                    withTimeout(5000) { holder.ttsState.first { it == PlaybackStatus.STOPPED } }
                }
            }
        }

    @Test fun replacingAndStoppingPreparationDidNotRestartStaleSpeech() =
        runBlocking<Unit> {
            val app = getApplicationContext<FeederApplication>()
            val holder: TTSStateHolder = app.di.direct.instance()
            ActivityScenario.launch(MainActivity::class.java).use {
                try {
                    withContext(Dispatchers.Main) {
                        holder.tts(listOf(AnnotatedString("An old article. ".repeat(100))), false, -40)
                    }
                    delay(150)
                    withContext(Dispatchers.Main) {
                        holder.tts(listOf(AnnotatedString("A replacement article.")), false, -41)
                    }
                    val replacement = withTimeout(60000) { holder.progress.first { it.articleId == -41L && (it.durationMillis > 0 || it.error != null) } }
                    assertNull(replacement.error)
                    withContext(Dispatchers.Main) { holder.stop() }
                    delay(500)
                    assertEquals(PlaybackStatus.STOPPED, holder.ttsState.value)
                    assertNull(holder.progress.value.articleId)
                } finally {
                    withContext(Dispatchers.Main) { holder.stop() }
                }
            }
        }
}
