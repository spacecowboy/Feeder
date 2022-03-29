package com.nononsenseapps.feeder.model

import java.io.InputStream
import org.jsoup.Jsoup

fun getPlainTextOfHtmlStream(
    inputStream: InputStream,
    baseUrl: String,
): String? =
    Jsoup.parse(inputStream, null, baseUrl)
        ?.body()
        ?.text()
