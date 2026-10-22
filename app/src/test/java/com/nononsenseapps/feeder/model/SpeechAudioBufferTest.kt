package com.nononsenseapps.feeder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SpeechAudioBufferTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun completionNotifiedEarlyAndLateListenersExactlyOnce() {
        val buffer = SpeechAudioBuffer(folder.newFile(), SpeechPcmFormat(24000, 1, 16, false))
        var early = 0
        var late = 0
        var removed = 0
        val removedListener = {
            removed++
            Unit
        }
        buffer.addCompletionListener { early++ }
        buffer.addCompletionListener(removedListener)
        buffer.removeCompletionListener(removedListener)
        buffer.publish(48000)
        assertEquals(0, early)
        buffer.publish(96000, finished = true)
        buffer.addCompletionListener { late++ }
        assertEquals(1, early)
        assertEquals(1, late)
        assertEquals(0, removed)
    }

    @Test(expected = IllegalStateException::class)
    fun completionWasTerminal() {
        val buffer = SpeechAudioBuffer(folder.newFile(), SpeechPcmFormat(24000, 1, 16, false))
        buffer.publish(48000, finished = true)
        buffer.publish(96000)
    }

    @Test(expected = IOException::class)
    fun cancellationWasTerminalForTheProducerToo() {
        val buffer = SpeechAudioBuffer(folder.newFile(), SpeechPcmFormat(24000, 1, 16, false))
        buffer.abort()
        buffer.publish(48000)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectedInvalidPcmFormat() {
        SpeechPcmFormat(0, 1, 16, false)
    }

    @Test fun readerWaitedAtFrontierUntilMoreAudioWasPublished() {
        val buffer = SpeechAudioBuffer(folder.newFile(), SpeechPcmFormat(24000, 1, 16, false))
        buffer.publish(48000)
        assertEquals(48000, buffer.available(0))
        val executor = Executors.newSingleThreadExecutor()
        try {
            val entered = CountDownLatch(1)
            val read =
                executor.submit<Int> {
                    entered.countDown()
                    buffer.available(48000)
                }
            assertTrue(entered.await(1, TimeUnit.SECONDS))
            assertFalse(read.isDone)
            buffer.publish(96000)
            assertEquals(48000, read.get(1, TimeUnit.SECONDS).toInt())
            buffer.publish(96000, finished = true)
            assertEquals(-1, buffer.available(96000))
            assertEquals(2_000_000L, buffer.format.timeUs(96000))
        } finally {
            executor.shutdownNow()
        }
    }

    @Test fun cancellationUnblockedReaderWithoutWaitingForAnotherUtterance() {
        val buffer = SpeechAudioBuffer(folder.newFile(), SpeechPcmFormat(24000, 1, 16, false))
        val executor = Executors.newSingleThreadExecutor()
        try {
            val read =
                executor.submit<Boolean> {
                    try {
                        buffer.available(0)
                        false
                    } catch (e: IOException) {
                        true
                    }
                }
            buffer.abort()
            assertTrue(read.get(1, TimeUnit.SECONDS))
        } finally {
            executor.shutdownNow()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectedPartialPcmFrames() {
        SpeechAudioBuffer(folder.newFile(), SpeechPcmFormat(24000, 2, 16, false)).publish(3)
    }
}
