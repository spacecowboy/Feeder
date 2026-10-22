package com.nononsenseapps.feeder.model

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.os.Bundle
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.Lifecycle
import androidx.media3.common.Player
import androidx.media3.session.CommandButton
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
    @Test fun playedBeforeSynthesisFinishedAndSystemStopCancelledGeneration() =
        runBlocking<Unit> {
            val app = getApplicationContext<FeederApplication>()
            val holder: TTSStateHolder = app.di.direct.instance()
            ActivityScenario.launch(MainActivity::class.java).use {
                var controller: MediaController? = null
                try {
                    withContext(Dispatchers.Main) {
                        holder.tts(listOf(AnnotatedString("This sentence is ready to read while more audio is generated. ".repeat(200))), false, -70)
                    }
                    val started =
                        withTimeout(60000) {
                            holder.progress.first { it.error != null || (it.articleId == -70L && it.positionMillis > 200 && it.isDurationEstimated) }
                        }
                    assertNull(started.error)
                    assertTrue(started.generatedMillis > 0)
                    assertTrue(started.durationMillis > started.generatedMillis)
                    assertTrue(started.segment != null)
                    assertTrue(started.followSpokenText)
                    withContext(Dispatchers.Main) {
                        holder.stopFollowingSpokenText()
                        assertEquals(false, holder.progress.value.followSpokenText)
                        assertEquals(-70L, holder.requestSpokenTextFocus())
                        assertTrue(holder.progress.value.followSpokenText)
                    }
                    val future =
                        withContext(Dispatchers.Main) {
                            MediaController.Builder(app, SessionToken(app, ComponentName(app, ReadAloudService::class.java))).buildAsync()
                        }
                    val remote = withContext(Dispatchers.IO) { future.get(10, TimeUnit.SECONDS) }
                    controller = remote
                    withContext(Dispatchers.Main) { remote.pause() }
                    withTimeout(5000) { holder.ttsState.first { it == PlaybackStatus.PAUSED } }
                    withContext(Dispatchers.Main) {
                        val stop = remote.mediaButtonPreferences.single { it.icon == CommandButton.ICON_STOP }
                        assertTrue(stop.slots.contains(CommandButton.SLOT_FORWARD_SECONDARY))
                        remote.sendCustomCommand(checkNotNull(stop.sessionCommand), Bundle.EMPTY)
                    }
                    withTimeout(5000) { holder.ttsState.first { it == PlaybackStatus.STOPPED } }
                    delay(300)
                    assertNull(holder.progress.value.articleId)
                } finally {
                    withContext(Dispatchers.Main) {
                        controller?.release()
                        holder.stop()
                    }
                }
            }
        }

    @Test fun replacingPreparedAudioKeptSynthesisInTheForeground() =
        runBlocking<Unit> {
            val app = getApplicationContext<FeederApplication>()
            val holder: TTSStateHolder = app.di.direct.instance()
            ActivityScenario.launch(MainActivity::class.java).use {
                try {
                    withContext(Dispatchers.Main) { holder.tts(listOf(AnnotatedString("A short article.")), false, -60) }
                    val prepared = withTimeout(60000) { holder.progress.first { it.articleId == -60L && (it.durationMillis > 0 || it.error != null) } }
                    assertNull(prepared.error)
                    withTimeout(5000) {
                        while (app.getSystemService(NotificationManager::class.java).activeNotifications.none {
                                it.notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION)
                            }
                        ) {
                            delay(50)
                        }
                    }
                    withContext(Dispatchers.Main) { holder.tts(listOf(AnnotatedString("A much longer replacement article. ".repeat(500))), false, -61) }
                    delay(500)
                    assertTrue(holder.progress.value.preparationProgress != null || holder.progress.value.isDurationEstimated)
                    @Suppress("DEPRECATION")
                    val service =
                        app
                            .getSystemService(ActivityManager::class.java)
                            .getRunningServices(Int.MAX_VALUE)
                            .first { service -> service.service.className == ReadAloudService::class.java.name }
                    assertTrue(service.foreground)
                    val future =
                        withContext(Dispatchers.Main) {
                            MediaController.Builder(app, SessionToken(app, ComponentName(app, ReadAloudService::class.java))).buildAsync()
                        }
                    val controller = withContext(Dispatchers.IO) { future.get(10, TimeUnit.SECONDS) }
                    try {
                        withContext(Dispatchers.Main) { controller.pause() }
                        withTimeout(5000) { holder.ttsState.first { it == PlaybackStatus.PAUSED } }
                    } finally {
                        withContext(Dispatchers.Main) { controller.release() }
                    }
                } finally {
                    withContext(Dispatchers.Main) { holder.stop() }
                }
            }
        }

    @Test fun immediatelyRestartingAfterStopKeptTheNewRequest() =
        runBlocking<Unit> {
            val app = getApplicationContext<FeederApplication>()
            val holder: TTSStateHolder = app.di.direct.instance()
            ActivityScenario.launch(MainActivity::class.java).use {
                try {
                    withContext(Dispatchers.Main) { holder.tts(listOf(AnnotatedString("An old article. ".repeat(100))), false, -50) }
                    delay(20)
                    withContext(Dispatchers.Main) {
                        holder.stop()
                        holder.tts(listOf(AnnotatedString("A new article after stopping.")), false, -51)
                    }
                    val prepared = withTimeout(60000) { holder.progress.first { it.articleId == -51L && (it.durationMillis > 0 || it.error != null) } }
                    assertNull(prepared.error)
                } finally {
                    withContext(Dispatchers.Main) { holder.stop() }
                }
            }
        }

    @Test fun synthesizedAudioSupportedSystemPauseSeekAndResume() =
        runBlocking<Unit> {
            val app = getApplicationContext<FeederApplication>()
            val holder: TTSStateHolder = app.di.direct.instance()
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                try {
                    withContext(Dispatchers.Main) {
                        holder.setLanguage(ForcedLocale(java.util.Locale.ENGLISH))
                        holder.tts(
                            listOf(AnnotatedString("This is the first sentence. This is the second sentence. This is the last sentence.")),
                            false,
                            -42,
                            "Read-aloud test",
                            "Test Author",
                        )
                    }
                    val prepared = withTimeout(60000) { holder.progress.first { (it.durationMillis > 0 && !it.isDurationEstimated) || it.error != null } }
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
                            assertEquals("Test Author", controller.mediaMetadata.artist.toString())
                            assertTrue(controller.mediaMetadata.artworkData?.isNotEmpty() == true)
                            assertEquals(10_000, controller.seekBackIncrement)
                            assertTrue(controller.isCommandAvailable(Player.COMMAND_SEEK_BACK))
                            assertTrue(
                                controller.mediaButtonPreferences.any {
                                    it.icon == CommandButton.ICON_SKIP_BACK_10 && it.slots.contains(CommandButton.SLOT_BACK)
                                },
                            )
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
                        withContext(Dispatchers.Main) { controller.stop() }
                        withTimeout(5000) { holder.ttsState.first { it == PlaybackStatus.PAUSED } }
                        withContext(Dispatchers.Main) { holder.play() }
                        withTimeout(5000) { holder.ttsState.first { it == PlaybackStatus.PLAYING } }
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
