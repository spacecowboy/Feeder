package com.nononsenseapps.feeder.util

import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
import android.net.Uri
import android.os.Build
import com.nononsenseapps.feeder.BuildConfig

private fun deviceInfoBlock(): String = """
    Application: ${BuildConfig.APPLICATION_ID} (flavor ${BuildConfig.BUILD_TYPE.ifBlank { "None" }})
    Version: ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})
    Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
    Device: ${Build.MANUFACTURER} ${Build.MODEL}
""".trimIndent()

private fun bugBody(): String =
    """
    ${deviceInfoBlock()}
    
    Hello.
    
    I'd like to report an issue:
    """.trimIndent()

private const val emailReportAddress: String = "feeder@nononsenseapps.com"

fun emailBugReportIntent(): Intent = Intent(ACTION_SENDTO).apply {
    data = Uri.parse("mailto:$emailReportAddress")
    putExtra(EXTRA_SUBJECT, "Bug report for Feeder")
    putExtra(EXTRA_EMAIL, emailReportAddress)
    putExtra(Intent.EXTRA_TEXT, bugBody())
}

private const val crashReportAddress: String = "crashes@nononsenseapps.com"

private fun crashBody(throwable: Throwable): String =
    """
    ${deviceInfoBlock()}
    
    Unhandled exception:
    
    ${throwable.stackTraceToString()}
    """.trimIndent()

fun emailCrashReportIntent(throwable: Throwable): Intent = Intent(ACTION_SENDTO).apply {
    data = Uri.parse("mailto:$crashReportAddress")
    putExtra(EXTRA_SUBJECT, "Crash report for Feeder")
    putExtra(EXTRA_EMAIL, crashReportAddress)
    putExtra(Intent.EXTRA_TEXT, crashBody(throwable))
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

fun openGitlabIssues(): Intent = Intent(ACTION_VIEW).also {
    it.data = Uri.parse("https://gitlab.com/spacecowboy/Feeder/issues")
}

const val KOFI_URL = "https://ko-fi.com/spacecowboy"

fun openKoFiIntent(): Intent = Intent(ACTION_VIEW).also {
    it.data = Uri.parse(KOFI_URL)
}
