package com.nononsenseapps.feeder.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.hasTextColor
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.util.Prefs
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

@RunWith(AndroidJUnit4::class)
@LargeTest
class AddFeedDialogThemeTest {
    @get:Rule
    var activityRule: ActivityTestRule<EditFeedActivity> = ActivityTestRule(EditFeedActivity::class.java, false, false)

    private val kodein by closestKodein(getApplicationContext() as Context)
    private val prefs by kodein.instance<Prefs>()

    @Test
    fun startsInDarkModeIfSet() {
        prefs.isNightMode = true

        activityRule.launchActivity(null)

        onView(withId(R.id.feed_url)).check(ViewAssertions.matches(hasTextColor(R.color.white_87)))
    }

    @Test
    fun startsInLightModeIfSet() {
        prefs.isNightMode = false

        activityRule.launchActivity(null)

        onView(withId(R.id.feed_url)).check(ViewAssertions.matches(hasTextColor(R.color.black_87)))
    }
}
