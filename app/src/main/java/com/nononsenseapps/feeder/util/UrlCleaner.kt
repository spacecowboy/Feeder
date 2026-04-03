package com.nononsenseapps.feeder.util

import android.net.Uri

/**
 * Strips common tracking query parameters from URLs before sharing.
 *
 * Removes standard UTM parameters and other well-known tracking tags
 * while preserving the rest of the URL.
 */
fun stripTrackingParameters(url: String): String {
    val uri =
        try {
            Uri.parse(url)
        } catch (_: Exception) {
            return url
        }

    val paramNames = uri.queryParameterNames
    if (paramNames.isEmpty()) return url

    val trackingPrefixes = listOf("utm_", "traffic_source")
    val hasTracking = paramNames.any { name -> trackingPrefixes.any { name.startsWith(it) } }
    if (!hasTracking) return url

    val builder = uri.buildUpon().clearQuery()
    for (name in paramNames) {
        if (trackingPrefixes.none { name.startsWith(it) }) {
            for (value in uri.getQueryParameters(name)) {
                builder.appendQueryParameter(name, value)
            }
        }
    }
    return builder.build().toString()
}
