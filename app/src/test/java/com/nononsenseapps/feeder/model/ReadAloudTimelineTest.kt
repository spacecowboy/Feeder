package com.nononsenseapps.feeder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale

class ReadAloudTimelineTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun splitSentencesPreservedTextAndRespectedEngineLimit() {
        val text = "First sentence. Second sentence! " + "a".repeat(2000) + " \uD83D\uDE00 End."
        val parts = speechSegments(text, Locale.ENGLISH, 100)
        assertTrue(parts.all { it.end - it.start <= 100 })
        assertEquals(text, parts.joinToString("") { text.substring(it.start, it.end) })
        assertTrue(parts.none { Character.isHighSurrogate(text[it.end - 1]) })
    }

    @Test fun blankTextHadNoSegments() {
        assertTrue(speechSegments(" \n\t", Locale.ENGLISH, 50).isEmpty())
    }

    @Test fun longOpeningSentenceUsedAShortStartupChunkWithoutLosingText() {
        val text = "A long opening sentence ".repeat(100) + "."
        val parts = speechSegments(text, Locale.ENGLISH, 700, firstChunkMaxLength = 180)
        assertTrue(parts.first().end <= 180)
        assertTrue(parts.drop(1).all { it.end - it.start <= 700 })
        assertEquals(text, parts.joinToString("") { text.substring(it.start, it.end) })
    }

    @Test fun seekingSelectedTheCorrectSentenceAtBoundaries() {
        val parts = listOf(SpeechSegment(0, 10, 0, 500), SpeechSegment(10, 20, 500, 1000))
        assertEquals(parts[0], parts.segmentAt(499))
        assertEquals(parts[1], parts.segmentAt(500))
        assertNull(parts.segmentAt(1000))
        assertNull(parts.segmentAt(-1))
    }

    @Test fun wrotePlayablePcmWaveHeaderAndExactDuration() {
        val file = folder.newFile()
        SpeechWaveFile(file).use {
            it.setFormat(24000, 1, 16, false)
            it.append(ByteArray(24000))
            it.append(ByteArray(24000))
            assertEquals(1000L, it.durationMillis())
            it.finish()
        }
        val bytes = file.readBytes()
        val header = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals("RIFF", String(bytes, 0, 4))
        assertEquals(48036, header.getInt(4))
        assertEquals("WAVE", String(bytes, 8, 4))
        assertEquals(24000, header.getInt(24))
        assertEquals(48000, header.getInt(40))
        assertEquals(48044, bytes.size)
    }

    @Test fun paddedOddSizedEightBitPcm() {
        val file = folder.newFile()
        SpeechWaveFile(file).use {
            it.setFormat(8000, 1, 8, false)
            it.append(byteArrayOf(1, 2, 3))
            it.finish()
        }
        assertEquals(48L, file.length())
        assertEquals(3, ByteBuffer.wrap(file.readBytes()).order(ByteOrder.LITTLE_ENDIAN).getInt(40))
    }

    @Test(expected = IllegalStateException::class)
    fun rejectedFormatChanges() {
        SpeechWaveFile(folder.newFile()).use {
            it.setFormat(24000, 1, 16, false)
            it.setFormat(48000, 1, 16, false)
        }
    }
}
