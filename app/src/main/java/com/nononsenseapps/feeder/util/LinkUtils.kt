package com.nononsenseapps.feeder.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.Locale

/**
 * Ensures a url is valid, having a scheme and everything. It turns 'google.com' into 'http://google.com' for example.
 */
fun sloppyLinkToStrictURL(url: String): URL {
    if (url.isBlank()) {
        throw MalformedURLException("String is blank")
    }
    return try {
        // If no exception, it's valid
        URL(url)
    } catch (_: MalformedURLException) {
        // Might be an unknown protocol in which case a URL is not valid to be created
        sloppyLinktoURIOrNull(url)?.let { uri ->
            if (uri.isAbsolute) {
                uri.toURL()
            } else {
                null
            }
        } ?: URL("http://$url") // No scheme, assume http
    }
}

fun sloppyLinktoURIOrNull(text: String): URI? =
    try {
        URI(text)
    } catch (_: URISyntaxException) {
        null
    }

fun sloppyLinkToStrictURLOrNull(url: String): URL? =
    try {
        sloppyLinkToStrictURL(url)
    } catch (_: MalformedURLException) {
        null
    }

private val LAN_HOST_SUFFIXES =
    arrayOf(".local", ".localhost", ".localdomain", ".home.arpa", ".lan", ".home", ".internal")

private const val LAN_DNS_TIMEOUT_MS = 2_000L

private fun URL.normalizedHost(): String =
    host
        ?.lowercase(Locale.ROOT)
        ?.removePrefix("[")
        ?.removeSuffix("]")
        ?.substringBefore('%')
        ?.trim('.')
        .orEmpty()

/**
 * True when the URL names a LAN-local host without a DNS lookup: localhost, a LAN suffix such as
 * .local or .lan, or a private, link-local, loopback or CGNAT IP literal.
 */
fun URL.isLanLocalAddress(): Boolean {
    val host = normalizedHost()
    if (host.isEmpty()) {
        return false
    }
    if (host == "localhost" || LAN_HOST_SUFFIXES.any { host.endsWith(it) }) {
        return true
    }
    if (!host.isNumericIpLiteral()) {
        return false
    }
    val address = runCatching { InetAddress.getByName(host) }.getOrNull() ?: return false
    return address.isLanLocal()
}

/**
 * Like [isLanLocalAddress], but also resolves a hostname and is true when any of its addresses is
 * LAN-local, so a bare name like `nas` or a DNS name pointing at a private address is caught.
 * A lookup that fails or takes longer than two seconds counts as not local.
 */
suspend fun URL.resolvesToLanLocalAddress(resolve: (String) -> Array<InetAddress> = InetAddress::getAllByName): Boolean {
    if (isLanLocalAddress()) {
        return true
    }
    val host = normalizedHost()
    if (host.isEmpty() || host.isNumericIpLiteral()) {
        return false
    }
    // Detached so a resolver stuck past the timeout does not hold up the caller
    val lookup = CoroutineScope(Dispatchers.IO).async { runCatching { resolve(host) }.getOrNull() }
    val addresses = withTimeoutOrNull(LAN_DNS_TIMEOUT_MS) { lookup.await() } ?: return false
    return addresses.any { it.isLanLocal() }
}

private fun InetAddress.isLanLocal(): Boolean {
    if (isLoopbackAddress || isLinkLocalAddress || isSiteLocalAddress) {
        return true
    }
    val bytes = address
    return when (bytes.size) {
        4 -> (bytes[0].toInt() and 0xff) == 100 && (bytes[1].toInt() and 0xff) in 64..127
        16 -> (bytes[0].toInt() and 0xfe) == 0xfc
        else -> false
    }
}

private fun String.isNumericIpLiteral(): Boolean {
    val octets = split('.')
    if (octets.size == 4 && octets.all { octet -> octet.toIntOrNull() in 0..255 }) {
        return true
    }
    return contains(':') && all { char -> char.isDigit() || char in 'a'..'f' || char == ':' || char == '.' }
}

fun urlHasNoAuthParams(url: URL): Boolean = url.userInfo == null || url.userInfo.isEmpty()

fun urlHasNoQueryParams(url: URL): Boolean = url.query?.isEmpty() != false

/**
 * Returns a URL but does not guarantee that it accurately represents the input string if the input string is an invalid URL.
 * This is used to ensure that migrations to versions where Feeds have URL and not strings don't crash.
 */
fun sloppyLinkToStrictURLNoThrows(url: String): URL =
    try {
        sloppyLinkToStrictURL(url)
    } catch (_: MalformedURLException) {
        URL("http://")
    }

/**
 * On error, this method simply returns the original link. It does *not* throw exceptions.
 */
fun relativeLinkIntoAbsolute(
    base: URL,
    link: String,
): String =
    try {
        // If no exception, it's valid
        relativeLinkIntoAbsoluteOrThrow(base, link).toString()
    } catch (_: MalformedURLException) {
        link
    }

/**
 * On error, this method simply returns the original link. It does *not* throw exceptions.
 */
fun relativeLinkIntoAbsoluteOrNull(
    base: URL,
    link: String?,
): String? =
    try {
        // If no exception, it's valid
        if (link != null) {
            relativeLinkIntoAbsoluteOrThrow(base, link).toString()
        } else {
            null
        }
    } catch (_: MalformedURLException) {
        link
    }

/**
 * On error, throws MalformedURLException.
 */
@Throws(MalformedURLException::class)
fun relativeLinkIntoAbsoluteOrThrow(
    base: URL,
    link: String,
): URL =
    try {
        // If no exception, it's valid
        URL(link)
    } catch (_: MalformedURLException) {
        URL(base, link)
    }

/**
 * On error, this method returns null. It does *not* throw exceptions.
 */
fun relativeLinkIntoAbsoluteOrNullIfNotValid(
    base: URL,
    link: String?,
): URL? =
    try {
        // If no exception, it's valid
        if (link != null) {
            relativeLinkIntoAbsoluteOrThrow(base, link)
        } else {
            null
        }
    } catch (_: MalformedURLException) {
        null
    }
