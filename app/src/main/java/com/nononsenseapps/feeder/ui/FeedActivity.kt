package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.base.DIAwareActivity
import com.nononsenseapps.feeder.model.FeedListViewModel
import com.nononsenseapps.feeder.model.FeedUnreadCount
import com.nononsenseapps.feeder.model.SettingsViewModel
import com.nononsenseapps.feeder.model.configurePeriodicSync
import com.nononsenseapps.feeder.model.isOkToSyncAutomatically
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.util.Prefs
import com.nononsenseapps.feeder.util.bundle
import kotlinx.android.synthetic.main.activity_navigation.drawer_layout
import kotlinx.android.synthetic.main.app_bar_navigation.fab
import kotlinx.android.synthetic.main.app_bar_navigation.toolbar
import kotlinx.android.synthetic.main.navdrawer_for_ab_overlay.*
import kotlinx.coroutines.launch
import org.kodein.di.instance

const val EXPORT_OPML_CODE = 101
const val IMPORT_OPML_CODE = 102
const val EDIT_FEED_CODE = 103

const val EXTRA_FEEDITEMS_TO_MARK_AS_NOTIFIED: String = "items_to_mark_as_notified"

class FeedActivity : DIAwareActivity() {
    private lateinit var navAdapter: FeedsAdapter
    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    private val prefs: Prefs by instance()
    private val settingsViewModel: SettingsViewModel by instance()
    private val feedListViewModel: FeedListViewModel by instance()
    private var restoredState: Boolean = false

    var fabOnClickListener: () -> Unit = {}

    init {
        lifecycleScope.launchWhenCreated {
            if (!restoredState) {
                handleSettingIntent()
            }
        }

        lifecycleScope.launchWhenStarted {
            // Enable periodic sync
            configurePeriodicSync(applicationContext, forceReplace = false)

            // Just so app label doesn't flicker in before label is set by feed fragment
            if (supportActionBar?.title == getString(R.string.app_name)) {
                supportActionBar?.title = ""
            }

            settingsViewModel.liveThemePreferenceNoInitial.observe(this@FeedActivity) {
                val intents = NavDeepLinkBuilder(this@FeedActivity)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.settingsFragment)
                    .createTaskStackBuilder()
                    .intents

                finish()
                startActivities(intents)
            }

            feedListViewModel.liveFeedsAndTagsWithUnreadCounts.observe(
                this@FeedActivity,
                androidx.lifecycle.Observer<List<FeedUnreadCount>> {
                    navAdapter.submitList(it)
                }
            )

            // When the user runs the app for the first time, we want to land them with the
            // navigation drawer open. But just the first time.
            if (!prefs.welcomeDone) {
                // first run of the app starts with the nav drawer open
                prefs.welcomeDone = true
                openNavDrawer()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        navController.saveState()?.let {
            outState.putAll(it)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme here - must be done before super call
        when (prefs.isNightMode) {
            true -> {
                R.style.AppThemeNight
            }
            false -> {
                R.style.AppThemeDay
            }
        }.let {
            setTheme(it)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        setSupportActionBar(toolbar)

        // Write default setting if method has never been called before
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)

        fab.setOnClickListener {
            fabOnClickListener()
        }

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val appBarConfiguration = AppBarConfiguration(navController.graph, drawer_layout)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        // Drawer stuff
        navdrawer_list.setHasFixedSize(true)
        navdrawer_list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        navAdapter = FeedsAdapter(object : OnNavigationItemClickListener {
            override fun onNavigationItemClick(id: Long, displayTitle: String?, url: String?, tag: String?) {
                drawer_layout.closeDrawer(GravityCompat.START)

                if (navController.currentDestination?.id == R.id.feedFragment) {
                    navController.navigate(
                        R.id.action_feedFragment_self,
                        bundle {
                            putLong(ARG_FEED_ID, id)
                            putString(ARG_FEED_TITLE, displayTitle)
                            putString(ARG_FEED_URL, url)
                            putString(ARG_FEED_TAG, tag)
                        }
                    )
                }
            }
        })
        navdrawer_list.adapter = navAdapter

        // Navigation stuff
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Drawer handling
            when (destination.id) {
                R.id.feedFragment -> {
                    drawer_layout.setDrawerLockMode(LOCK_MODE_UNLOCKED)
                }
                else -> {
                    drawer_layout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
                }
            }

            // Fab handling
            when (destination.id) {
                R.id.feedFragment -> {
                    fab.setImageResource(R.drawable.ic_done_all_white_24dp)
                    if (prefs.show_fab) {
                        fab.show()
                    } else {
                        fab.hide()
                    }
                }
                else -> {
                    fab.hide()
                    fabOnClickListener = {}
                }
            }

            // Toolbar hiding
            when (destination.id) {
                R.id.feedFragment -> fixedToolbar()
                R.id.readerFragment -> hideableToolbar()
                R.id.readerWebViewFragment -> fixedToolbar()
                else -> fixedToolbar()
            }
        }

        savedInstanceState?.let {
            restoredState = true
            navController.restoreState(it)
        }

    }

    private fun hideableToolbar() {
        toolbar?.layoutParams = toolbar.layoutParams.also {
            if (it is AppBarLayout.LayoutParams) {
                it.scrollFlags = SCROLL_FLAG_SNAP or SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS
            }
        }
    }

    private fun fixedToolbar() {
        toolbar?.layoutParams = toolbar.layoutParams.also {
            if (it is AppBarLayout.LayoutParams) {
                it.scrollFlags = 0
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleSettingIntent()
    }

    private fun handleSettingIntent() {
        if (intent?.action == Intent.ACTION_MANAGE_NETWORK_USAGE) {
            navController.navigate(R.id.settingsFragment)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            val handled: Boolean = supportFragmentManager
                .primaryNavigationFragment
                ?.childFragmentManager
                ?.primaryNavigationFragment.let {
                    when (it) {
                        is ReaderWebViewFragment -> it.goBack()
                        else -> false
                    }
                }

            if (!handled) {
                super.onBackPressed()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        syncFeedsMaybe()
        setNightModeViewModelState()
    }

    private fun syncFeedsMaybe() = lifecycleScope.launch {
        if (prefs.syncOnResume) {
            if (isOkToSyncAutomatically(applicationContext)) {
                requestFeedSync(
                    di = di,
                    ignoreConnectivitySettings = false,
                    forceNetwork = false,
                    parallell = true
                )
            }
        }
    }

    fun openNavDrawer() {
        drawer_layout.openDrawer(GravityCompat.START)
    }

    private fun setNightModeViewModelState() {
        settingsViewModel.liveIsNightMode.value = prefs.isNightMode
    }
}
