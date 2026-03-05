package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.nononsenseapps.feeder.background.runOnceRssSync
import com.nononsenseapps.feeder.background.runOnceSyncChainGetUpdates
import com.nononsenseapps.feeder.background.schedulePeriodicOrphanedFilesCleanup
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.notifications.NotificationsWorker
import com.nononsenseapps.feeder.ui.compose.navigation.AddFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.ArticleDestination
import com.nononsenseapps.feeder.ui.compose.navigation.EditFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.FeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SearchFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SettingsDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SyncScreenDestination
import com.nononsenseapps.feeder.ui.compose.navigation.TextSettingsDestination
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
        maybeRequestSync()
    }

    private fun maybeRequestSync() =
        lifecycleScope.launch {
            if (mainActivityViewModel.shouldSyncOnResume) {
                if (mainActivityViewModel.isOkToSyncAutomatically()) {
                    runOnceSyncChainGetUpdates(di)
                    runOnceRssSync(
                        di = di,
                        forceNetwork = false,
                        triggeredByUser = false,
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityViewModel.ensurePeriodicSyncConfigured()

        // Configure daily cleanup of orphaned article files
        schedulePeriodicOrphanedFilesCleanup(di)

        enableEdgeToEdge()

        setContent {
            withAllProviders {
                AppContent()
            }
        }
    }

    @Composable
    fun AppContent() {
        val navController = rememberNavController()
        val navDrawerListState = rememberLazyListState()

        NavHost(navController, startDestination = FeedDestination.route) {
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
            // Add Fonts
            TextSettingsDestination.register(this, navController, navDrawerListState)
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
        private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
    }
}
