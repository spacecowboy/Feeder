package com.nononsenseapps.feeder.model

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.nononsenseapps.feeder.db.COL_LINK
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RssNotificationsKtTest {
    @Test
    fun openInBrowserIntentPointsToActivityWithIdAndLink() {
        val intent: Intent = getOpenInDefaultActivityIntent(getInstrumentation().context, 99, "http://foo")

        assertEquals("com.nononsenseapps.feeder.ui.OpenInWebBrowserActivity",
                intent.component?.className)
        assertEquals(Uri.withAppendedPath(URI_FEEDITEMS, "99"), intent.data)
        assertEquals("http://foo", intent.getStringExtra(COL_LINK))
    }
}
