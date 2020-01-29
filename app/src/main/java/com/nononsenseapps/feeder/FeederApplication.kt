package com.nononsenseapps.feeder

import android.app.Application
import android.content.ContentResolver
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedDao
import com.nononsenseapps.feeder.db.room.FeedItemDao
import com.nononsenseapps.feeder.di.networkModule
import com.nononsenseapps.feeder.di.stateModule
import com.nononsenseapps.feeder.di.viewModelModule
import com.nononsenseapps.feeder.model.UserAgentInterceptor
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.ToastMaker
import com.nononsenseapps.jsonfeed.cachingHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.io.File
import java.security.Security

@ExperimentalCoroutinesApi
@Suppress("unused")
class FeederApplication : MultiDexApplication(), KodeinAware {
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
                    cacheDirectory = externalCacheDir ?: filesDir
            ).newBuilder()
                    .addNetworkInterceptor(UserAgentInterceptor)
                    .build()
        }
        import(networkModule)
        import(stateModule)
    }

    init {
        // Install Conscrypt to handle missing SSL cyphers on older platforms
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        }
    }

    override fun onCreate() {
        super.onCreate()
        staticFilesDir = filesDir
    }

    companion object {
        // Needed for database migration
        lateinit var staticFilesDir: File
    }
}
