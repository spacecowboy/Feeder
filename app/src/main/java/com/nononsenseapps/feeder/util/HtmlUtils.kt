package com.nononsenseapps.feeder.util

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser.EXTRA_CREATE_NEW_TAB
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.model.FeederService
import com.nononsenseapps.feeder.model.getColorCompat
import java.util.regex.Pattern

//val patternImgSrc = Pattern.compile("img\b.*?src=\"(.*?)\"")
private val patternImgSrc: Pattern = Pattern.compile("img.*?src=[\"'](.*?)[\"']")

fun naiveFindImageLink(text: String?): String? {
    if (text != null) {
        val matcher = patternImgSrc.matcher(text)

        if (matcher?.find() == true) {
            return matcher.group(1).toString()
        }
    }

    return null
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

fun openLinkInCustomTab(context: Context, link: String, feedItemId: Long?): Boolean {
    try {
        val intent = CustomTabsIntent.Builder().apply {
            setToolbarColor(context.getColorCompat(R.color.primary))

            addDefaultShareMenuItem()

            if (feedItemId != null) {
                addMenuItem(
                        context.getString(R.string.mark_as_unread),
                        PendingIntent.getService(
                                context,
                                0,
                                FeederService.getIntentForId(context, feedItemId),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        )
                )
            }
        }.build()
        intent.launchUrl(context, Uri.parse(link))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.no_activity_for_link, Toast.LENGTH_SHORT).show()
        return false
    }
    return true
}
