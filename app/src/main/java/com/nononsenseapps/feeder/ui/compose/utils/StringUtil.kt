/**
 * The contents of this file originated at org.jsoup.internal.StringUtil.
 *
 * Since it's not public, it's been copied here.
 */
package com.nononsenseapps.feeder.ui.compose.utils

import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Pattern

/**
 * Create a new absolute URL, from a provided existing absolute URL and a relative URL component.
 * @param baseUrl the existing absolute base URL
 * @param relUrl the relative URL to resolve. (If it's already absolute, it will be returned)
 * @return an absolute URL if one was able to be generated, or the empty string if not
 */
fun resolve(
    baseUrl: String,
    relUrl: String,
): String {
    // workaround: java will allow control chars in a path URL and may treat as relative, but Chrome / Firefox will strip and may see as a scheme. Normalize to browser's view.
    val baseUrl = stripControlChars(baseUrl)
    val relUrl = stripControlChars(relUrl)
    return try {
        val base: URL =
            try {
                URL(baseUrl)
            } catch (e: MalformedURLException) {
                // the base is unsuitable, but the attribute/rel may be abs on its own, so try that
                val abs = URL(relUrl)
                return abs.toExternalForm()
            }
        resolve(base, relUrl).toExternalForm()
    } catch (e: MalformedURLException) {
        // it may still be valid, just that Java doesn't have a registered stream handler for it, e.g. tel
        // we test here vs at start to normalize supported URLs (e.g. HTTP -> http)
        if (validUriScheme.matcher(relUrl).find()) relUrl else ""
    }
}

private val validUriScheme = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+-.]*:")

private val controlChars = Pattern.compile("[\\x00-\\x1f]*") // matches ascii 0 - 31, to strip from url

private fun stripControlChars(input: String): String {
    return controlChars.matcher(input).replaceAll("")
}

private val extraDotSegmentsPattern = Pattern.compile("^/((\\.{1,2}/)+)")

/**
 * Create a new absolute URL, from a provided existing absolute URL and a relative URL component.
 * @param base the existing absolute base URL
 * @param relUrl the relative URL to resolve. (If it's already absolute, it will be returned)
 * @return the resolved absolute URL
 * @throws MalformedURLException if an error occurred generating the URL
 */
@Throws(MalformedURLException::class)
fun resolve(
    base: URL,
    relUrl: String,
): URL {
    var relUrl = stripControlChars(relUrl)
    // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
    if (relUrl.startsWith("?")) {
        relUrl = base.path + relUrl
    }
    // workaround: //example.com + ./foo = //example.com/./foo, not //example.com/foo
    val url = URL(base, relUrl)
    var fixedFile = extraDotSegmentsPattern.matcher(url.file).replaceFirst("/")
    if (url.ref != null) {
        fixedFile = fixedFile + "#" + url.ref
    }
    return URL(url.protocol, url.host, url.port, fixedFile)
}
