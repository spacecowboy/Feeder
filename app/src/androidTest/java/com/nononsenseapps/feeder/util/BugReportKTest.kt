package com.nononsenseapps.feeder.util

import android.content.Intent
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.net.Uri
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.nononsenseapps.feeder.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@MediumTest
class BugReportKTest {
    @Test
    fun bodyContainsAndroidInformation() {
        assertEquals("""
            ${BuildConfig.APPLICATION_ID} (flavor ${BuildConfig.FLAVOR.ifBlank { "None" }})
            version ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})
            on Android ${Build.VERSION.RELEASE} (SDK-${Build.VERSION.SDK_INT})

            Describe your issue and how to reproduce it below:
        """.trimIndent(),
                emailBody())
    }

    @Test
    fun subjectIsSensible() {
        assertEquals(
                "Bug report for Feeder",
                emailSubject())
    }

    @Test
    fun emailAddressIsCorrect() {
        assertEquals(
                "jonas.feederbugs@cowboyprogrammer.org",
                emailReportAddress()
        )
    }

    @Test
    fun intentIsCorrect() {
        val intent = emailBugReportIntent()

        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals(Uri.parse("mailto:${emailReportAddress()}"), intent.data)
        assertEquals(emailSubject(), intent.getStringExtra(EXTRA_SUBJECT))
        assertEquals(emailBody(), intent.getStringExtra(EXTRA_TEXT))
        assertEquals(emailReportAddress(), intent.getStringExtra(EXTRA_EMAIL))
    }
}
