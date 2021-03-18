package com.nononsenseapps.feeder.ui

object MockResponses {

    val cowboyprogrammer_feed_json_headers = mapOf(
        "cache-control" to "public",
        "content-type" to "application/json",
        "date" to "Tue, 30 Oct 2018 14:25:58 GMT",
        "etag" to "W/\"5b6ca19c-146ab\"",
        "expires" to "Tue, 30 Oct 2018 15:25:58 GMT",
        "last-modified" to "Thu, 09 Aug 2018 20:18:36 GMT",
        "vary" to "Accept-Encoding"
    )

    val cowboy_feed_json_body: String
        get() = String(javaClass.getResourceAsStream("cowboy_feed.json")!!.readBytes())
}
