package com.nononsenseapps.feeder.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FeedItemSQLTest {
    @Test
    fun getDomain() {
        val fi1 = FeedItemSQL(link = "https://www.cowboyprogrammer.org/some/path.txt")
        assertEquals("cowboyprogrammer.org", fi1.domain)

        val fi2 = FeedItemSQL(enclosurelink = "https://www.cowboyprogrammer.org/some/path.txt")
        assertEquals("cowboyprogrammer.org", fi2.domain)

        val fi3 = FeedItemSQL(enclosurelink = "asdff\\asdf")
        assertEquals(null, fi3.domain)
    }

    @Test
    fun getEnclosureFilename() {
        val fi1 = FeedItemSQL(enclosurelink = "https://www.cowboyprogrammer.org/some/file.txt")
        assertEquals("file.txt", fi1.enclosureFilename)

        val fi2 = FeedItemSQL(enclosurelink = "https://www.cowboyprogrammer.org/some/file.txt?param=2")
        assertEquals("file.txt", fi2.enclosureFilename)

        val fi3 = FeedItemSQL(enclosurelink = "https://www.cowboyprogrammer.org/some%20file.txt")
        assertEquals("some file.txt", fi3.enclosureFilename)

        val fi4 = FeedItemSQL(enclosurelink = "https://www.cowboyprogrammer.org")
        assertEquals(null, fi4.enclosureFilename)
    }

    @Test
    fun magnetLinkGivesNullFilename() {
        val fi = FeedItemSQL(enclosurelink = "magnet:?xt=urn:btih:E6F5537982306CF703E5016B2BBD36C9B3E3CDD0&dn=Game+of+Thrones+S07E01+PROPER+WEBRip+x264+RARBG&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=http%3A%2F%2Ftracker.trackerfix.com%3A80%2Fannounce")
        assertNull(fi.enclosureFilename)
    }
}