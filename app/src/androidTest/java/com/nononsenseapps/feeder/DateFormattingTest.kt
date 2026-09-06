package com.nononsenseapps.feeder

import android.content.Context
import android.content.res.Configuration
import android.os.ParcelFileDescriptor
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nononsenseapps.feeder.archmodel.formatForFeed
import com.nononsenseapps.feeder.ui.compose.feedarticle.formatArticleDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DateFormattingTest {
    private lateinit var context: Context
    private var originalTimeFormat: String? = null

    @Before
    fun setUp() {
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()
        val configuration = Configuration(applicationContext.resources.configuration).apply { setLocale(Locale.US) }
        context = applicationContext.createConfigurationContext(configuration)
        originalTimeFormat = Settings.System.getString(context.contentResolver, Settings.System.TIME_12_24)
    }

    @After
    fun tearDown() {
        setTimeFormat(originalTimeFormat)
    }

    @Test
    fun feedAndWidgetDatesUseClockPreferenceAndLocalTimeZone() {
        val zoneId = ZoneId.of("Asia/Kolkata")
        val today = LocalDate.of(2026, 7, 20)
        val publicationDate = ZonedDateTime.parse("2026-07-19T23:30:00Z")

        setTimeFormat("24")
        assertEquals("05:00", publicationDate.formatForFeed(context, zoneId, today))

        setTimeFormat("12")
        val twelveHourTime = publicationDate.formatForFeed(context, zoneId, today)
        assertTrue(twelveHourTime.contains("5:00"))
        assertTrue(twelveHourTime.contains("AM"))

        val olderPublicationDate = ZonedDateTime.parse("2026-07-18T23:30:00Z")
        assertEquals("Jul 19, 2026", olderPublicationDate.formatForFeed(context, zoneId, today))
        assertEquals("", null.formatForFeed(context, zoneId, today))
    }

    @Test
    fun articleDateUsesClockPreferenceWithFullLocalizedDate() {
        val publicationDate = ZonedDateTime.parse("2026-07-20T12:35:00Z")
        val zoneId = ZoneId.of("Asia/Kolkata")

        setTimeFormat("24")
        val twentyFourHourDate = formatArticleDate(context, publicationDate, zoneId)
        assertTrue(twentyFourHourDate.contains("Monday"))
        assertTrue(twentyFourHourDate.contains("July"))
        assertTrue(twentyFourHourDate.contains("20"))
        assertTrue(twentyFourHourDate.contains("2026"))
        assertTrue(twentyFourHourDate.contains("18:05"))
        assertFalse(twentyFourHourDate.contains("PM"))

        setTimeFormat("12")
        val twelveHourDate = formatArticleDate(context, publicationDate, zoneId)
        assertTrue(twelveHourDate.contains("6:05"))
        assertTrue(twelveHourDate.contains("PM"))
        assertEquals("", formatArticleDate(context, null, zoneId))
    }

    private fun setTimeFormat(value: String?) {
        val command =
            if (value == null) {
                "settings delete system ${Settings.System.TIME_12_24}"
            } else {
                "settings put system ${Settings.System.TIME_12_24} $value"
            }
        val output = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        ParcelFileDescriptor.AutoCloseInputStream(output).use { it.readBytes() }
    }
}
