package com.nononsenseapps.feeder.util

import java.net.MalformedURLException
import java.net.URL

/**
 * On error, this method simply returns the original link. It does *not* throw exceptions.
 */
fun relativeLinkIntoAbsolute(base: String, link: String = ""): String = try {
    // If no exception, it's valid
    relativeLinkIntoAbsoluteOrThrow(base, link)
} catch (_: MalformedURLException) {
    link
}

/**
 * On error, throws MalformedURLException.
 */
@Throws(MalformedURLException::class)
fun relativeLinkIntoAbsoluteOrThrow(base: String, link: String = ""): String = try {
    // If no exception, it's valid
    URL(link).toString()
} catch (_: MalformedURLException) {
    try {
        val baseUrl = URL(base)

        URL(baseUrl, link).toString()
    } catch (_: MalformedURLException) {
        val baseUrl = URL("http://$base")

        // maybe throws MalformedURLException
        URL(baseUrl, link).toString()
    }
}
