package com.nononsenseapps.feeder.db.room

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.*
import org.junit.Test

class ConvertersTest {
    @Test
    fun zeroIs1970() {
        assertEquals(DateTime(0, DateTimeZone.UTC),
                Converters().dateTimeFromLong(0))
    }

    @Test
    fun negativeLongGivesValidDate() {
        assertEquals(DateTime(-1, DateTimeZone.UTC),
                Converters().dateTimeFromLong(-1))
    }

    @Test
    fun noLongGivesNullDate() {
        assertNull(Converters().dateTimeFromLong(null))
    }

    @Test
    fun noDateTimeGivesNull() {
        assertNull(Converters().longFromDateTime(null))
    }

    @Test
    fun dateTimeGivesLong() {
        assertEquals(1514768461000,
                Converters().longFromDateTime(DateTime.parse("2018-01-01T01:01:01Z")))
    }
}