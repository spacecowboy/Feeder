package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
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
import com.nononsenseapps.feeder.ui.compose.utils.withAllProviders
import kotlinx.coroutines.launch
import org.kodein.di.instance

class MainActivity : DIAwareComponentActivity() {
    private val notificationsWorker: NotificationsWorker by instance()
    private val mainActivityViewModel: MainActivityViewModel by instance(arg = this)

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

    private fun maybeRequestSync() =
        lifecycleScope.launch {
            if (mainActivityViewModel.shouldSyncOnResume) {
                if (mainActivityViewModel.isOkToSyncAutomatically()) {
                    requestFeedSync(
                        di = di,
                        forceNetwork = false,
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installExceptionHandler()

        mainActivityViewModel.ensurePeriodicSyncConfigured()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            withAllProviders {
                AppContent()
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun AppContent() {
        val navController = rememberAnimatedNavController()
        val navDrawerListState = rememberLazyListState()

        AnimatedNavHost(navController, startDestination = FeedDestination.route) {
            FeedDestination.register(this, navController, navDrawerListState)
            ArticleDestination.register(this, navController, navDrawerListState)
            // Feed editing
            EditFeedDestination.register(this, navController, navDrawerListState)
            SearchFeedDestination.register(this, navController, navDrawerListState)
            AddFeedDestination.register(this, navController, navDrawerListState)
            // Settings
            SettingsDestination.register(this, navController, navDrawerListState)
            // Sync settings
            SyncScreenDestination.register(this, navController, navDrawerListState)
        }

        DisposableEffect(navController) {
            val listener =
                Consumer<Intent> { intent ->
                    if (!navController.handleDeepLink(intent)) {
                        Log.e(LOG_TAG, "NavController rejected intent: $intent")
                    }
                }
            addOnNewIntentListener(listener)
            onDispose { removeOnNewIntentListener(listener) }
        }
    }

    companion object {
        private const val LOG_TAG = "FEEDER_MAIN"
    }
}
