package com.nononsenseapps.feeder.util

import android.content.Intent.ACTION_VIEW
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@MediumTest
class BugReportKTest {
    @Test
    fun issuesIntentIsCorrect() {
        val intent = openGithubIssues()

        assertEquals(ACTION_VIEW, intent.action)
        assertEquals(Uri.parse("https://github.com/spacecowboy/feeder/issues"), intent.data)
    }
}
