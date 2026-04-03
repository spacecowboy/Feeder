package com.nononsenseapps.feeder.util

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Strips common tracking query parameters from URLs before sharing.
 *
 * Removes standard UTM parameters and other well-known tracking tags
 * while preserving the rest of the URL.
 */
fun stripTrackingParameters(url: String): String {
    val uri =
        try {
            URI(url)
        } catch (_: Exception) {
            return url
        }

    val query = uri.rawQuery ?: return url

    val trackingPrefixes = listOf("utm_", "traffic_source")

    val params =
        query.split("&").map { param ->
            val parts = param.split("=", limit = 2)
            val name = URLDecoder.decode(parts[0], "UTF-8")
            val value = if (parts.size > 1) parts[1] else ""
            name to value
        }

    val hasTracking = params.any { (name, _) -> trackingPrefixes.any { name.startsWith(it) } }
    if (!hasTracking) return url

    val filtered = params.filter { (name, _) -> trackingPrefixes.none { name.startsWith(it) } }

    val newQuery =
        if (filtered.isEmpty()) {
            null
        } else {
            filtered.joinToString("&") { (name, value) ->
                val encodedName = URLEncoder.encode(name, "UTF-8")
                if (value.isEmpty()) encodedName else "$encodedName=$value"
            }
        }

    return URI(uri.scheme, uri.authority, uri.path, newQuery, uri.fragment).toString()
}
