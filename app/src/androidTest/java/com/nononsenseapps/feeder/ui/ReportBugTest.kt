package com.nononsenseapps.feeder.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.Prefs
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

@RunWith(AndroidJUnit4::class)
@LargeTest
class ReportBugTest {
    @get:Rule
    val activityRule: ActivityTestRule<FeedActivity> = ActivityTestRule(FeedActivity::class.java)

    private val kodein by closestKodein(getApplicationContext() as Context)
    private val prefs by kodein.instance<Prefs>()

    @Before
    fun keepNavDrawerClosed() {
        prefs.welcomeDone = true
    }

    @Test
    fun clickingReportBugOpensEmailComposer() {
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText(R.string.send_bug_report)).perform(click())

        // Can't assert anything except that it didn't crash
        assertNotNull(activityRule.activity)
    }
}
