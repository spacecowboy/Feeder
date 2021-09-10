package com.nononsenseapps.feeder.model

import org.jsoup.Jsoup
import java.io.InputStream

fun getPlainTextOfHtmlStream(
    inputStream: InputStream,
    baseUrl: String,
): String? =
    Jsoup.parse(inputStream, null, baseUrl)
        ?.body()
        ?.let { body ->
            body.text()
        }
