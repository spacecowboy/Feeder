package com.nononsenseapps.feeder.ui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.nononsenseapps.feeder.archmodel.Repository
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
import com.nononsenseapps.feeder.util.isLanLocalAddress
import com.nononsenseapps.feeder.util.resolvesToLanLocalAddress
import kotlinx.coroutines.launch
import org.kodein.di.instance

class MainActivity : DIAwareComponentActivity() {
    private val notificationsWorker: NotificationsWorker by instance()
    private val mainActivityViewModel: MainActivityViewModel by instance(arg = this)
    private val repository: Repository by instance()
    private val preferences: SharedPreferences by instance()
    private var localNetworkMigrationStarted = false
    private val localNetworkPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                runOnceRssSync(
                    di = di,
                    forceNetwork = false,
                    triggeredByUser = false,
                )
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
        maybeRequestLocalNetworkPermissionForExistingFeeds()
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?,
    ): Boolean {
        if (mainActivityViewModel.isPagingMode.value && repository.isArticleOpen.value) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    mainActivityViewModel.emitScrollCommand(ScrollDirection.UP)
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    mainActivityViewModel.emitScrollCommand(ScrollDirection.DOWN)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
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

    private fun maybeRequestLocalNetworkPermissionForExistingFeeds() {
        if (localNetworkMigrationStarted ||
            Build.VERSION.SDK_INT < 37 ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED ||
            preferences.getBoolean(KEY_LOCAL_NETWORK_PERMISSION_REQUESTED, false)
        ) {
            return
        }
        localNetworkMigrationStarted = true
        lifecycleScope.launch {
            if (preferences.getBoolean(KEY_LOCAL_NETWORK_PERMISSION_REQUESTED, false)) {
                return@launch
            }
            val feedUrls = repository.getFeedsOrderedByUrl().map { it.url }
            // Literal LAN hosts first, so the DNS lookups only run when none of those match
            val hasLocalFeed =
                feedUrls.any { it.isLanLocalAddress() } ||
                    feedUrls.distinctBy { it.host }.any { it.resolvesToLanLocalAddress() }
            if (!hasLocalFeed) {
                preferences.edit { putBoolean(KEY_LOCAL_NETWORK_PERMISSION_REQUESTED, true) }
                return@launch
            }
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                localNetworkMigrationStarted = false
                return@launch
            }
            // Persist before launching so denial and activity recreation do not prompt again.
            preferences.edit { putBoolean(KEY_LOCAL_NETWORK_PERMISSION_REQUESTED, true) }
            localNetworkPermissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
    }

    @Composable
    fun AppContent() {
        val navController = rememberNavController()
        val navDrawerListState = rememberLazyListState()

        NavHost(navController, startDestination = FeedDestination.route) {
            FeedDestination.register(this, navController, navDrawerListState, mainActivityViewModel)
            ArticleDestination.register(this, navController, navDrawerListState, mainActivityViewModel)
            // Feed editing
            EditFeedDestination.register(this, navController, navDrawerListState, mainActivityViewModel)
            SearchFeedDestination.register(this, navController, navDrawerListState, mainActivityViewModel)
            AddFeedDestination.register(this, navController, navDrawerListState, mainActivityViewModel)
            // Settings
            SettingsDestination.register(this, navController, navDrawerListState, mainActivityViewModel)
            // Sync settings
            SyncScreenDestination.register(this, navController, navDrawerListState, mainActivityViewModel)
            // Add Fonts
            TextSettingsDestination.register(this, navController, navDrawerListState, mainActivityViewModel)
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
        private const val KEY_LOCAL_NETWORK_PERMISSION_REQUESTED = "local_network_permission_requested"
    }
}
