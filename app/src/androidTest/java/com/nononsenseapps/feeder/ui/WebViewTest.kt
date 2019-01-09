package com.nononsenseapps.feeder.ui

import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.nononsenseapps.feeder.R
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
        assertEquals(
                activityRule.activity.findViewById<View>(R.id.toolbar_actionbar).height ,
                activityRule.activity.supportFragmentManager.fragments.first().view!!.paddingTop,
                message = "Web view should have padding top equal to action bar size"
        )
    }
}
