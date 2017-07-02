package com.nononsenseapps.feeder.db

import org.junit.Test

import org.junit.Assert.*

/**
 * Created by jonas on 17/07/02.
 */
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

}