package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.model.workmanager.isOkToSyncAutomatically
import com.nononsenseapps.feeder.model.workmanager.requestFeedSync
import com.nononsenseapps.feeder.model.workmanager.scheduleGetUpdates
import com.nononsenseapps.feeder.notifications.NotificationsWorker
import com.nononsenseapps.feeder.ui.compose.navigation.AddFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.ArticleDestination
import com.nononsenseapps.feeder.ui.compose.navigation.EditFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.FeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SearchFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SettingsDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SyncScreenDestination
import com.nononsenseapps.feeder.ui.compose.theme.FeederTheme
import com.nononsenseapps.feeder.ui.compose.theme.ProvideFontScale
import com.nononsenseapps.feeder.ui.compose.utils.withWindowSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import org.unifiedpush.android.connector.UnifiedPush.getDistributor
import org.unifiedpush.android.connector.UnifiedPush.getDistributors
import org.unifiedpush.android.connector.UnifiedPush.registerApp
import org.unifiedpush.android.connector.UnifiedPush.saveDistributor

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
        scheduleGetUpdates(di)
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

        val selectedDistributor = getDistributor(this)
        Log.d("UNIFIED", "distributor: $selectedDistributor")
        if (selectedDistributor.isNotEmpty()) {
            // Re-register in case something broke
            Log.d("UNIFIED", "registerApp")
            registerApp(this)
        } else {
            val distributors = getDistributors(this)
            Log.d("UNIFIED", "distributors: ${distributors.joinToString(", ")}")
            val chosenDistributor = "io.heckel.ntfy"
            Log.d("UNIFIED", "saveDistributor: $chosenDistributor")
            saveDistributor(this, chosenDistributor)
            Log.d("UNIFIED", "registerApp")
            registerApp(this)
        }

        runBlocking(Dispatchers.IO) {
            sendPushMessage("https://arstechnica.com/feed/FOOhttps://arstechnica.com/?p=1901755")
        }

        setContent {
            val currentTheme by mainActivityViewModel.currentTheme.collectAsState()
            val darkThemePreference by mainActivityViewModel.darkThemePreference.collectAsState()
            val dynamicColors by mainActivityViewModel.dynamicColors.collectAsState()
            val textScale by mainActivityViewModel.textScale.collectAsState()

            FeederTheme(
                currentTheme = currentTheme,
                darkThemePreference = darkThemePreference,
                dynamicColors = dynamicColors,
            ) {
                withDI {
                    withWindowSize {
                        ProvideFontScale(fontScale = textScale) {
                            appContent()
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun appContent() {
        val navController = rememberAnimatedNavController().also {
            if (this.navController == null) {
                this.navController = it
            }
        }

        AnimatedNavHost(navController, startDestination = FeedDestination.route) {
            FeedDestination.register(this, navController)
            ArticleDestination.register(this, navController)
            // Feed editing
            EditFeedDestination.register(this, navController)
            SearchFeedDestination.register(this, navController)
            AddFeedDestination.register(this, navController)
            // Settings
            SettingsDestination.register(this, navController)
            // Sync settings
            SyncScreenDestination.register(this, navController)
        }
    }
}
