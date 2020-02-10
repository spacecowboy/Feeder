package com.nononsenseapps.feeder.di

import com.nononsenseapps.feeder.model.FeedParser
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.JsonFeedParser
import com.nononsenseapps.jsonfeed.feedAdapter
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.FlowPreview
import okhttp3.OkHttpClient
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider

@FlowPreview
val networkModule = Kodein.Module(name = "network") {
    // Parsers can carry state so safer to use providers
    bind<JsonAdapter<Feed>>() with provider { feedAdapter() }
    bind<JsonFeedParser>() with provider { JsonFeedParser(instance<OkHttpClient>(), instance()) }
    bind<FeedParser>() with provider { FeedParser(kodein) }
}
