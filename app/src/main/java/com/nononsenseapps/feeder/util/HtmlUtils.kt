package com.nononsenseapps.feeder.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser.EXTRA_CREATE_NEW_TAB
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabsIntent
import com.nononsenseapps.feeder.R
import org.jsoup.Jsoup
import org.jsoup.parser.Parser.unescapeEntities

fun findFirstImageLinkInHtml(text: String?, baseUrl: String?): String? =
    if (text != null) {
        val doc = unescapeEntities(text, true).byteInputStream().use {
            Jsoup.parse(it, "UTF-8", baseUrl ?: "")
        }

        doc.getElementsByTag("img").asSequence()
            .filterNot { it.attr("width") == "1" || it.attr("height") == "1" }
            .map {
                // abs: will resolve relative urls against the baseurl - and non-url value will get
                // dropped, such as invalid values and data/base64 values
                it.attr("abs:src")
            }
            .firstOrNull {
                it.isNotBlank()
                        && !it.contains("twitter_icon", ignoreCase = true)
                        && !it.contains("facebook_icon", ignoreCase = true)
            }
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
