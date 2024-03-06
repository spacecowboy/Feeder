package com.nononsenseapps.feeder.model.gofeed

import com.nononsenseapps.feeder.sync.adapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException

class GoFeedAdapter {
    private val moshi =
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

    private val goFeedAdapter = moshi.adapter<GoFeed>()

    @Throws(IOException::class)
    private fun fromJson(json: ByteArray): GoFeed? {
        return goFeedAdapter.fromJson(json.decodeToString())
    }

    @Throws(IOException::class)
    fun parseBody(body: String): GoFeed? {
        return gofeedandroid.Gofeedandroid.parseBodyString(body)?.let {
            return fromJson(it)
        }
    }

    @Throws(IOException::class)
    fun parseBody(body: ByteArray): GoFeed? {
        return gofeedandroid.Gofeedandroid.parseBodyBytes(body)?.let {
            return fromJson(it)
        }
    }
}
