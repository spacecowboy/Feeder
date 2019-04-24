package com.nononsenseapps.feeder.util

import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Build
import com.nononsenseapps.feeder.BuildConfig
import com.nononsenseapps.feeder.R

internal fun emailSubject(): String = "Bug report for Feeder"

internal fun emailBody(isTablet: Boolean): String = """
            ${BuildConfig.APPLICATION_ID} (flavor ${BuildConfig.FLAVOR.ifBlank { "None" }})
            version ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})
            on Android ${Build.VERSION.RELEASE} (SDK-${Build.VERSION.SDK_INT})
            on a Tablet? ${
if (isTablet) {
    "Yes"
} else {
    "No"
}}

            Describe your issue and how to reproduce it below:
        """.trimIndent()

internal fun emailReportAddress(): String = "jonas.feederbugs@cowboyprogrammer.org"

fun emailBugReportIntent(context: Context?): Intent = Intent(Intent.ACTION_SENDTO).also {
    it.putExtra(EXTRA_SUBJECT, emailSubject())
    it.putExtra(EXTRA_TEXT, emailBody(context?.resources?.getBoolean(R.bool.isTablet) ?: false))
    it.putExtra(EXTRA_EMAIL, emailReportAddress())
    it.data = Uri.parse("mailto:${emailReportAddress()}")
}
