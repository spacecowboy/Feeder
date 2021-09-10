package com.nononsenseapps.feeder.util

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser.EXTRA_CREATE_NEW_TAB
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabsIntent
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.FeederService

// Using negative lookahead to skip data: urls, being inline base64
// And capturing original quote to use as ending quote
private val regexImgSrc = """img.*?src=(["'])((?!data).*?)\1""".toRegex(RegexOption.DOT_MATCHES_ALL)

fun naiveFindImageLink(text: String?): String? =
    if (text != null) {
        regexImgSrc.find(text)?.groupValues?.get(2)
    } else {
        null
    }

/**
 * Opens the specified link in a new tab in the browser, or otherwise suitable application. If no suitable application
 * could be found, a Toast is displayed and false is returned. Otherwise, true is returned.
 */
fun openLinkInBrowser(context: Context, link: String): Boolean {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        intent.putExtra(EXTRA_CREATE_NEW_TAB, true)
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.no_activity_for_link, Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}

fun openLinkInCustomTab(
    context: Context,
    link: String,
    @ColorInt toolbarColor: Int,
): Boolean {
    try {
        val intent = CustomTabsIntent.Builder().apply {
            setToolbarColor(toolbarColor)
            addDefaultShareMenuItem()
        }.build()
        intent.launchUrl(context, Uri.parse(link))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.no_activity_for_link, Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}
