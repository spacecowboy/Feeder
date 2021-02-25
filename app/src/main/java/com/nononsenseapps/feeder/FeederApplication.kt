package com.nononsenseapps.feeder

import android.app.Application
import android.content.ContentResolver
import android.content.SharedPreferences
import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.di.networkModule
import com.nononsenseapps.feeder.di.stateModule
import com.nononsenseapps.feeder.di.viewModelModule
import com.nononsenseapps.feeder.model.UserAgentInterceptor
import com.nononsenseapps.feeder.util.AsyncImageLoader
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.ToastMaker
import com.nononsenseapps.jsonfeed.cachingHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.io.File
import java.util.concurrent.TimeUnit

@FlowPreview
@ExperimentalCoroutinesApi
@Suppress("unused")
class FeederApplication : MultiDexApplication(), KodeinAware {
    private val applicationCoroutineScope = ApplicationCoroutineScope()

    override val kodein by Kodein.lazy {
        //import(androidXModule(this@FeederApplication))

        bind<Application>() with singleton { this@FeederApplication }
        bind<AppDatabase>() with singleton { AppDatabase.getInstance(this@FeederApplication) }
        bind<FeedDao>() with singleton { instance<AppDatabase>().feedDao() }
        bind<FeedItemDao>() with singleton { instance<AppDatabase>().feedItemDao() }

        import(viewModelModule)

        bind<WorkManager>() with singleton { WorkManager.getInstance(this@FeederApplication) }
        bind<ContentResolver>() with singleton { contentResolver }
        bind<ToastMaker>() with singleton {
            object : ToastMaker {
                override suspend fun makeToast(text: String) = withContext(Dispatchers.Main) {
                    Toast.makeText(this@FeederApplication, text, Toast.LENGTH_SHORT).show()
                }
            }
        }
        bind<NotificationManagerCompat>() with singleton { NotificationManagerCompat.from(this@FeederApplication) }
        bind<SharedPreferences>() with singleton { PreferenceManager.getDefaultSharedPreferences(this@FeederApplication) }
        bind<Prefs>() with singleton { Prefs(kodein) }

        bind<OkHttpClient>() with singleton {
            cachingHttpClient(
                cacheDirectory = (externalCacheDir ?: filesDir).resolve("http")
            ).newBuilder()
                .addNetworkInterceptor(UserAgentInterceptor)
                .build()
        }
        bind<ImageLoader>() with singleton {
            val prefs = instance<Prefs>()
            val okHttpClient = instance<OkHttpClient>()
                .newBuilder()
                // Use separate image cache or images will quickly evict feed caches
                .cache(Cache((externalCacheDir ?: filesDir).resolve("img"), 20L * 1024L * 1024L))
                .addInterceptor { chain ->
                    chain.proceed(
                        when (prefs.shouldLoadImages()) {
                            true -> chain.request()
                            false -> {
                                // Forces only cached responses to be used - if no cache then 504 is thrown
                                chain.request().newBuilder()
                                    .cacheControl(
                                        CacheControl.Builder()
                                            .onlyIfCached()
                                            .maxStale(Int.MAX_VALUE, TimeUnit.SECONDS)
                                            .maxAge(Int.MAX_VALUE, TimeUnit.SECONDS)
                                            .build()
                                    )
                                    .build()
                            }
                        }
                    )
                }
                .build()

            ImageLoader.Builder(instance())
                .okHttpClient(okHttpClient = okHttpClient)
                .componentRegistry {
                    add(SvgDecoder(applicationContext))
                    if (SDK_INT >= 28) {
                        add(ImageDecoderDecoder())
                    } else {
                        add(GifDecoder())
                    }
                }
                .build()
        }
        bind<AsyncImageLoader>() with singleton { AsyncImageLoader(kodein) }
        bind<ApplicationCoroutineScope>() with instance(applicationCoroutineScope)
        import(networkModule)
        import(stateModule)
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        staticFilesDir = filesDir
    }

    override fun onTerminate() {
        applicationCoroutineScope.cancel("Application is being terminated")
        super.onTerminate()
    }

    companion object {
        // Needed for database migration
        lateinit var staticFilesDir: File
    }
}
