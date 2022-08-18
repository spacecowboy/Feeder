package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.LocalImageLoader
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.model.isOkToSyncAutomatically
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.notifications.NotificationsWorker
import com.nononsenseapps.feeder.ui.compose.navigation.AddFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.ArticleDestination
import com.nononsenseapps.feeder.ui.compose.navigation.EditFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.FeedArticleDestination
import com.nononsenseapps.feeder.ui.compose.navigation.FeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SearchFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SettingsDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SyncScreenDestination
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.utils.rememberWindowSizeClass
import kotlinx.coroutines.launch
import org.kodein.di.compose.withDI
import org.kodein.di.instance

class MainActivity : DIAwareComponentActivity() {
    private val notificationsWorker: NotificationsWorker by instance()
    private val mainActivityViewModel: MainActivityViewModel by instance(arg = this)

    // This reference is only used for intent navigation
    private var navController: NavController? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let {
            if (navController?.handleDeepLink(intent) != true) {
                Log.e("FeederMainActivity", "In onNewIntent, navController rejected the intent")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        notificationsWorker.runForever()
    }

    override fun onStop() {
        notificationsWorker.stopForever()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        mainActivityViewModel.setResumeTime()
        maybeRequestSync()
    }

    private fun maybeRequestSync() = lifecycleScope.launch {
        if (mainActivityViewModel.shouldSyncOnResume) {
            if (isOkToSyncAutomatically(applicationContext)) {
                requestFeedSync(
                    di = di,
                    forceNetwork = false,
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityViewModel.ensurePeriodicSyncConfigured()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val currentTheme by mainActivityViewModel.currentTheme.collectAsState()
            val darkThemePreference by mainActivityViewModel.darkThemePreference.collectAsState()
            val dynamicColors by mainActivityViewModel.dynamicColors.collectAsState()

            FeederTheme(
                currentTheme = currentTheme,
                darkThemePreference = darkThemePreference,
                dynamicColors = dynamicColors,
            ) {
                withDI {
                    val imageLoader: ImageLoader by instance()
                    CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                        appContent()
                    }
                }
            }
        }
    }

    @Composable
    fun appContent() {
        val windowSize = rememberWindowSizeClass()

        val navController = rememberNavController().also {
            if (this.navController == null) {
                this.navController = it
            }
        }

        NavHost(navController, startDestination = FeedArticleDestination.route) {
            FeedArticleDestination.register(this, navController, windowSize)
            // Deep links
            FeedDestination.register(this, navController, windowSize)
            ArticleDestination.register(this, navController, windowSize)
            // Feed editing
            EditFeedDestination.register(this, navController, windowSize)
            SearchFeedDestination.register(this, navController, windowSize)
            AddFeedDestination.register(this, navController, windowSize)
            // Settings
            SettingsDestination.register(this, navController, windowSize)
            // Sync settings
            SyncScreenDestination.register(this, navController, windowSize)
        }
    }
}
