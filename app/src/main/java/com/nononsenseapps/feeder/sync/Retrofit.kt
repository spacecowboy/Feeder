package com.nononsenseapps.feeder.sync

import android.util.Log
import com.nononsenseapps.feeder.db.room.SyncRemote
import java.net.URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

fun getFeederSyncClient(
    syncRemote: SyncRemote,
    okHttpClient: OkHttpClient
): FeederSync {
    val moshi = getMoshi()

    val retrofit = Retrofit.Builder()
        .client(
            okHttpClient.newBuilder()
                .addInterceptor { chain ->
                    // TODO add authentication header - hardcoded but just so you can't map the API
                    // without also reading the source code
                    val response = chain.proceed(chain.request())
                    val isCachedResponse =
                        response.cacheResponse != null && (response.networkResponse == null || response.networkResponse?.code == 304)
                    Log.v(
                        "FEEDER_SYNC_CLIENT",
                        "Response cached: $isCachedResponse, ${response.networkResponse?.code}, cache-Control: ${response.cacheControl}"
                    )
//                    Log.v(
//                        "FEEDER_SYNC_CLIENT",
//                        "${response.headers}"
//                    )
                    response
                }
                .build()
        )
        .baseUrl(URL(syncRemote.url, "/api/"))
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    return retrofit.create()
}
