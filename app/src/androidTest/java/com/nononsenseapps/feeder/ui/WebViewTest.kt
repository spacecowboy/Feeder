package com.nononsenseapps.feeder.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class WebViewTest {
    @Rule
    @JvmField
    var activityRule: ActivityTestRule<ReaderWebViewActivity> = ActivityTestRule(ReaderWebViewActivity::class.java)

    @Test
    fun activityStarts() {
        assertNotNull(activityRule.activity)
    }

    @Test
    fun webViewHasPaddingToOffsetActionBar() {
        val actionBarSize = activityRule.activity?.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))?.let { attributes ->
            attributes.getDimensionPixelSize(0, 0).also {
                attributes.recycle()
            }
        }

        assertEquals(actionBarSize,
                activityRule.activity.supportFragmentManager.fragments.first().view!!.paddingTop,
                message = "Web view should have padding top equal to action bar size")
    }
}
