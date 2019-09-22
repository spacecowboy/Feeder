package com.nononsenseapps.feeder

import android.app.Application
import android.content.ContentResolver
import android.content.SharedPreferences
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.nononsenseapps.feeder.base.KodeinAwareViewModelFactory
import com.nononsenseapps.feeder.base.bindWithKodeinAwareViewModelFactory
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.model.*
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.ToastMaker
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.JsonFeedParser
import com.nononsenseapps.jsonfeed.cachingHttpClient
import com.nononsenseapps.jsonfeed.feedAdapter
import com.squareup.moshi.JsonAdapter
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import java.security.Security

@Suppress("unused")
class FeederApplication : MultiDexApplication(), KodeinAware {
    override val kodein by Kodein.lazy {
        //import(androidXModule(this@FeederApplication))

        bind<Application>() with singleton { this@FeederApplication }
        bind<AppDatabase>() with singleton { AppDatabase.getInstance(this@FeederApplication) }
        bind<FeedDao>() with singleton { instance<AppDatabase>().feedDao() }
        bind<FeedItemDao>() with singleton { instance<AppDatabase>().feedItemDao() }

        bind<KodeinAwareViewModelFactory>() with singleton { KodeinAwareViewModelFactory(kodein) }
        bindWithKodeinAwareViewModelFactory<FeedViewModel>()
        bindWithKodeinAwareViewModelFactory<FeedItemViewModel>()
        bindWithKodeinAwareViewModelFactory<FeedItemsViewModel>()
        bindWithKodeinAwareViewModelFactory<FeedListViewModel>()
        bindWithKodeinAwareViewModelFactory<SettingsViewModel>()

        bind<WorkManager>() with singleton { WorkManager.getInstance(this@FeederApplication) }
        bind<LocalBroadcastManager>() with singleton { LocalBroadcastManager.getInstance(this@FeederApplication) }
        bind<ContentResolver>() with singleton { contentResolver }
        bind<ToastMaker>() with singleton {
            object : ToastMaker {
                override fun makeToast(text: String) {
                    Toast.makeText(this@FeederApplication, text, Toast.LENGTH_SHORT).show()
                }
            }
        }
        bind<NotificationManagerCompat>() with singleton { NotificationManagerCompat.from(this@FeederApplication) }
        bind<SharedPreferences>() with singleton { PreferenceManager.getDefaultSharedPreferences(this@FeederApplication) }
        bind<Prefs>() with singleton { Prefs(kodein) }

        bind<OkHttpClient>() with singleton { cachingHttpClient(
                cacheDirectory = externalCacheDir ?: filesDir
        ) }
        // Parsers can carry state so safer to use providers
        bind<JsonAdapter<Feed>>() with provider { feedAdapter() }
        bind<JsonFeedParser>() with provider { JsonFeedParser(instance<OkHttpClient>(), instance()) }
        bind<FeedParser>() with provider { FeedParser(kodein) }
    }

    init {
        // Install Conscrypt to handle missing SSL cyphers on older platforms
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }
}
