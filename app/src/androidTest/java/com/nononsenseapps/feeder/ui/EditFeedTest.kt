package com.nononsenseapps.feeder.ui

import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class EditFeedTest {
    @Rule
    @JvmField
    var activityRule: ActivityTestRule<EditFeedActivity> = ActivityTestRule(EditFeedActivity::class.java)

    @Test
    fun activityStarts() {
        assertNotNull(activityRule.activity)
    }
}
