package com.nononsenseapps.feeder.util

import android.content.Intent.ACTION_SENDTO
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
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
        assertEquals(
            """
            ${BuildConfig.APPLICATION_ID} (flavor ${BuildConfig.BUILD_TYPE.ifBlank { "None" }})
            version ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})
            on Android ${Build.VERSION.RELEASE} (SDK-${Build.VERSION.SDK_INT})
            on a Tablet? No

            Describe your issue and how to reproduce it below:
            """.trimIndent(),
            emailBody(false)
        )
    }

    @Test
    fun bodyContainsAndroidInformationAsTablet() {
        assertEquals(
            """
            ${BuildConfig.APPLICATION_ID} (flavor ${BuildConfig.BUILD_TYPE.ifBlank { "None" }})
            version ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})
            on Android ${Build.VERSION.RELEASE} (SDK-${Build.VERSION.SDK_INT})
            on a Tablet? Yes

            Describe your issue and how to reproduce it below:
            """.trimIndent(),
            emailBody(true)
        )
    }

    @Test
    fun subjectIsSensible() {
        assertEquals(
            "Bug report for Feeder",
            emailSubject()
        )
    }

    @Test
    fun emailAddressIsCorrect() {
        assertEquals(
            "jonas.feederbugs@cowboyprogrammer.org",
            emailReportAddress()
        )
    }

    @Test
    fun emailIntentIsCorrect() {
        val intent = emailBugReportIntent()

        assertEquals(ACTION_SENDTO, intent.action)
        assertEquals(Uri.parse("mailto:${emailReportAddress()}"), intent.data)
        assertEquals(emailSubject(), intent.getStringExtra(EXTRA_SUBJECT))
        assertEquals(emailReportAddress(), intent.getStringExtra(EXTRA_EMAIL))
    }

    @Test
    fun issuesIntentIsCorrect() {
        val intent = openGitlabIssues()

        assertEquals(ACTION_VIEW, intent.action)
        assertEquals(Uri.parse("https://gitlab.com/spacecowboy/Feeder/issues"), intent.data)
    }
}
