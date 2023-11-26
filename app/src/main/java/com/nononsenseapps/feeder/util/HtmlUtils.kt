package com.nononsenseapps.feeder.util

import org.jsoup.Jsoup
import org.jsoup.parser.Parser.unescapeEntities

fun findFirstImageLinkInHtml(
    text: String?,
    baseUrl: String?,
): String? =
    if (text != null) {
        val doc =
            unescapeEntities(text, true).byteInputStream().use {
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
                it.isNotBlank() &&
                    !it.contains("twitter_icon", ignoreCase = true) &&
                    !it.contains("facebook_icon", ignoreCase = true)
            }
    } else {
        null
    }
