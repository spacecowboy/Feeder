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
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import com.jakewharton.threetenabp.AndroidThreeTen
import com.nononsenseapps.feeder.archmodel.Repository
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.db.room.ReadStatusSyncedDao
import com.nononsenseapps.feeder.db.room.RemoteFeedDao
import com.nononsenseapps.feeder.db.room.RemoteReadMarkDao
import com.nononsenseapps.feeder.db.room.SyncDeviceDao
import com.nononsenseapps.feeder.db.room.SyncRemoteDao
import com.nononsenseapps.feeder.di.androidModule
import com.nononsenseapps.feeder.di.archModelModule
import com.nononsenseapps.feeder.di.networkModule
import com.nononsenseapps.feeder.model.TTSStateHolder
import com.nononsenseapps.feeder.model.UserAgentInterceptor
import com.nononsenseapps.feeder.notifications.NotificationsWorker
import com.nononsenseapps.feeder.ui.compose.coil.TooLargeImageInterceptor
import com.nononsenseapps.feeder.util.ToastMaker
import com.nononsenseapps.feeder.util.currentlyUnmetered
import com.nononsenseapps.jsonfeed.cachingHttpClient
import java.io.File
import java.security.Security
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton

@Suppress("unused")
class FeederApplication : MultiDexApplication(), DIAware, ImageLoaderFactory {
    private val applicationCoroutineScope = ApplicationCoroutineScope()
    private val ttsStateHolder = TTSStateHolder(this, applicationCoroutineScope)

    override val di by DI.lazy {
        // import(androidXModule(this@FeederApplication))

        bind<Application>() with singleton { this@FeederApplication }
        bind<AppDatabase>() with singleton { AppDatabase.getInstance(this@FeederApplication) }
        bind<FeedDao>() with singleton { instance<AppDatabase>().feedDao() }
        bind<FeedItemDao>() with singleton { instance<AppDatabase>().feedItemDao() }
        bind<SyncRemoteDao>() with singleton { instance<AppDatabase>().syncRemoteDao() }
        bind<ReadStatusSyncedDao>() with singleton { instance<AppDatabase>().readStatusSyncedDao() }
        bind<RemoteReadMarkDao>() with singleton { instance<AppDatabase>().remoteReadMarkDao() }
        bind<RemoteFeedDao>() with singleton { instance<AppDatabase>().remoteFeedDao() }
        bind<SyncDeviceDao>() with singleton { instance<AppDatabase>().syncDeviceDao() }

        import(androidModule)

        import(archModelModule)

        bind<WorkManager>() with singleton { WorkManager.getInstance(this@FeederApplication) }
        bind<ContentResolver>() with singleton { contentResolver }
        bind<ToastMaker>() with singleton {
            object : ToastMaker {
                override suspend fun makeToast(text: String) = withContext(Dispatchers.Main) {
                    Toast.makeText(this@FeederApplication, text, Toast.LENGTH_SHORT).show()
                }

                override suspend fun makeToast(resId: Int) {
                    Toast.makeText(this@FeederApplication, resId, Toast.LENGTH_SHORT).show()
                }
            }
        }
        bind<NotificationManagerCompat>() with singleton { NotificationManagerCompat.from(this@FeederApplication) }
        bind<SharedPreferences>() with singleton {
            PreferenceManager.getDefaultSharedPreferences(
                this@FeederApplication
            )
        }

        bind<OkHttpClient>() with singleton {
            cachingHttpClient(
                cacheDirectory = (externalCacheDir ?: filesDir).resolve("http")
            ).newBuilder()
                .addNetworkInterceptor(UserAgentInterceptor)
                .build()
        }
        bind<ImageLoader>() with singleton {
            val repository = instance<Repository>()
            val okHttpClient = instance<OkHttpClient>()
                .newBuilder()
                // This is not used by Coil but no need to risk evicting the real cache
                .cache(Cache((externalCacheDir ?: filesDir).resolve("dummy_img"), 1024L))
                .addInterceptor { chain ->
                    chain.proceed(
                        when (!repository.loadImageOnlyOnWifi.value || currentlyUnmetered(this@FeederApplication)) {
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
                .diskCache(
                    DiskCache.Builder()
                        .directory((externalCacheDir ?: filesDir).resolve("image_cache"))
                        .maxSizeBytes(250L * 1024 * 1024)
                        .build()
                )
                .components {
                    add(TooLargeImageInterceptor())
                    add(SvgDecoder.Factory())
                    if (SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .build()
        }
        bind<ApplicationCoroutineScope>() with instance(applicationCoroutineScope)
        import(networkModule)
        bind<TTSStateHolder>() with instance(ttsStateHolder)
        bind<NotificationsWorker>() with singleton { NotificationsWorker(di) }
    }

    init {
        // Install Conscrypt to handle TLSv1.3 pre Android10
        Security.insertProviderAt(Conscrypt.newProvider(), 1)
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        staticFilesDir = filesDir
    }

    override fun onTerminate() {
        applicationCoroutineScope.cancel("Application is being terminated")
        ttsStateHolder.shutdown()
        super.onTerminate()
    }

    companion object {
        // Needed for database migration
        lateinit var staticFilesDir: File
    }

    override fun newImageLoader(): ImageLoader {
        return di.direct.instance()
    }
}
