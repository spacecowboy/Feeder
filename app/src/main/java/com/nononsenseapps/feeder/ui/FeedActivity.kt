package com.nononsenseapps.feeder.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.text.Html.fromHtml
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckedTextView
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.Background
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.Util
import com.nononsenseapps.feeder.model.RssSyncAdapter
import com.nononsenseapps.feeder.model.opml.exportOpmlInBackground
import com.nononsenseapps.feeder.model.opml.importOpmlInBackground
import com.nononsenseapps.feeder.ui.filepicker.MyFilePickerActivity
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.markItemsAsNotified
import com.nononsenseapps.feeder.util.requestFeedSync
import com.nononsenseapps.filepicker.AbstractFilePickerActivity
import kotlinx.coroutines.experimental.launch
import java.io.File

private const val EXPORT_OPML_CODE = 101
private const val IMPORT_OPML_CODE = 102
private const val EDIT_FEED_CODE = 103

const val EXTRA_FEEDITEMS_TO_MARK_AS_NOTIFIED: String = "items_to_mark_as_notified"

class FeedActivity : BaseActivity() {
    private val fragmentTag = "single_pane"

    private var fragment: Fragment? = null
    private lateinit var emptyView: View

    private val syncReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Load first feed if nothing is showing (could have been empty and now content has been loaded)
            when (intent.action) {
                RssSyncAdapter.SYNC_BROADCAST -> showAllFeeds(false)
                RssSyncAdapter.FEED_ADDED_BROADCAST -> {
                    if (fragment == null && intent.getLongExtra(COL_ID, -1) > 0) {
                        onNavigationDrawerItemSelected(intent.getLongExtra(COL_ID, -1), "", "", null)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        initializeActionBar()
        overridePendingTransition(0, 0)

        doFromNotificationActions()

        if (savedInstanceState == null) {
            fragment = defaultFragment()
            if (fragment == null) {
                showAllFeeds(false)
            } else {
                supportFragmentManager.beginTransaction().add(R.id.container, fragment, fragmentTag).commit()
            }
        } else {
            fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        }

        // Empty view
        emptyView = findViewById(android.R.id.empty)
        emptyView.visibility = if (fragment == null) View.VISIBLE else View.GONE

        val emptyAddFeed = findViewById<TextView>(R.id.empty_add_feed)
        @Suppress("DEPRECATION")
        emptyAddFeed.text = fromHtml(getString(R.string.empty_no_feeds_add))
        emptyAddFeed.setOnClickListener {
            startActivityForResult(Intent(this@FeedActivity, EditFeedActivity::class.java), EDIT_FEED_CODE)
        }

        // Night mode
        val nightCheck = findViewById<CheckedTextView>(R.id.nightcheck)
        nightCheck.isChecked = PrefUtils.isNightMode(this)
        nightCheck.setOnClickListener {
            // Toggle icon first
            nightCheck.toggle()
            // Toggle prefs second
            PrefUtils.setNightMode(this@FeedActivity, nightCheck.isChecked)
            // Change background
            setNightBackground()
        }

        // Database upgrade wipes all items, so request a one-time sync on start up
        if (PrefUtils.isFirstBootAfterDatabaseUpgrade(this)) {
            // Sync all feeds
            contentResolver.requestFeedSync()
            PrefUtils.markFirstBootAfterDatabaseUpgradeDone(this)
        }
    }

    private fun doFromNotificationActions() {
        val itemIdsToMarkAsNotified = intent?.getLongArrayExtra(EXTRA_FEEDITEMS_TO_MARK_AS_NOTIFIED)
        val contentResolver = contentResolver
        if (itemIdsToMarkAsNotified != null && contentResolver != null) {
            launch(Background) {
                contentResolver.markItemsAsNotified(itemIdsToMarkAsNotified)
            }
        }
    }

    private fun defaultFragment(): Fragment? {
        val lastTag = PrefUtils.getLastOpenFeedTag(this)
        val lastId = PrefUtils.getLastOpenFeedId(this)

        val intentId: Long? = intent?.data?.lastPathSegment?.toLong()

        // Will load title and url in fragment
        return if (intentId != null) {
            FeedFragment.newInstance(intentId,
                    intent?.extras?.getString(ARG_FEED_TITLE) ?: "",
                    intent?.extras?.getString(ARG_FEED_URL) ?: "",
                    intent?.extras?.getString(ARG_FEED_TAG, lastTag) ?: lastTag)
        } else if (lastTag != null || lastId > 0) {
            FeedFragment.newInstance(lastId, "", "", lastTag)
        } else {
            FeedFragment.newInstance(-10, null, null, null)
        }
    }

    fun showAllFeeds(overrideCurrent: Boolean = false) {
        if (fragment == null || overrideCurrent) {
            onNavigationDrawerItemSelected(-10, null, null, null)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        registerHideableHeaderView(findViewById(R.id.headerbar))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.feed, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                startActivityForResult(Intent(this@FeedActivity, EditFeedActivity::class.java), EDIT_FEED_CODE)
                true
            }
            R.id.action_opml_export -> {
                // Choose file, then export
                val intent: Intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.type = "text/opml"
                    intent.putExtra(Intent.EXTRA_TITLE, "feeder.opml")
                } else {
                    intent = Intent(this, MyFilePickerActivity::class.java)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_MODE, AbstractFilePickerActivity.MODE_NEW_FILE)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_ALLOW_EXISTING_FILE, true)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_START_PATH,
                            File(Environment.getExternalStorageDirectory(), "feeder.opml").path)
                }
                startActivityForResult(intent, EXPORT_OPML_CODE)
                true
            }
            R.id.action_opml_import -> {
                // Choose file
                val intent: Intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "*/*"
                    intent.putExtra(Intent.EXTRA_MIME_TYPES,
                            Util.ToStringArray("text/plain", "text/xml", "text/opml", "*/*"))
                } else {
                    intent = Intent(this, MyFilePickerActivity::class.java)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_SINGLE_CLICK, true)
                }
                startActivityForResult(intent, IMPORT_OPML_CODE)
                true
            }
            R.id.action_debug_log -> {
                startActivity(Intent(this, DebugLogActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationDrawerItemSelected(id: Long, title: String?, url: String?, tag: String?) {
        // update the main content by replacing fragments
        emptyView.visibility = View.GONE
        fragment = FeedFragment.newInstance(id, title, url, tag)
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragmentTag).commit()
        // Remember choice in future
        PrefUtils.setLastOpenFeed(this, id, tag)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(syncReceiver, IntentFilter(RssSyncAdapter.SYNC_BROADCAST))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(syncReceiver)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            EXPORT_OPML_CODE -> {
                val uri: Uri? = data?.data
                if (uri != null) {
                    exportOpmlInBackground(this, data.data)
                }
            }
            IMPORT_OPML_CODE -> {
                val uri: Uri? = data?.data
                if (uri != null) {
                    importOpmlInBackground(this, uri)
                }
            }
            EDIT_FEED_CODE -> {
                val id = data?.data?.lastPathSegment?.toLong()
                if (id != null) {
                    val lastTag = PrefUtils.getLastOpenFeedTag(this)

                    val fragment = FeedFragment.newInstance(id,
                            data.extras?.getString(ARG_FEED_TITLE) ?: "",
                            data.extras?.getString(ARG_FEED_URL) ?: "",
                            data.extras?.getString(ARG_FEED_TAG) ?: lastTag)

                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragmentTag).commitAllowingStateLoss()
                }
            }
        }
    }
}
