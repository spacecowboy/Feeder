package com.nononsenseapps.feeder.util

import java.net.MalformedURLException
import java.net.URL

/**
 * Ensures a url is valid, having a scheme and everything. It turns 'google.com' into 'http://google.com' for example.
 */
fun sloppyLinkToStrictURL(url: String): URL = try {
    // If no exception, it's valid
    URL(url)
} catch (_: MalformedURLException) {
    URL("http://$url")
}

/**
 * On error, this method simply returns the original link. It does *not* throw exceptions.
 */
fun relativeLinkIntoAbsolute(base: String, link: String): String = try {
    // If no exception, it's valid
    relativeLinkIntoAbsoluteOrThrow(base, link)
} catch (_: MalformedURLException) {
    link
}

/**
 * On error, throws MalformedURLException.
 */
@Throws(MalformedURLException::class)
fun relativeLinkIntoAbsoluteOrThrow(base: String, link: String): String = try {
    // If no exception, it's valid
    URL(link).toString()
} catch (_: MalformedURLException) {
    val baseUrl = sloppyLinkToStrictURL(base)
    URL(baseUrl, link).toString()
}
