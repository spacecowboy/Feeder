package com.nononsenseapps.feeder.ui

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.CoroutineScopedFragment
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.*
import com.nononsenseapps.feeder.model.opml.exportOpml
import com.nononsenseapps.feeder.model.opml.importOpml
import com.nononsenseapps.feeder.ui.filepicker.MyFilePickerActivity
import com.nononsenseapps.feeder.util.*
import com.nononsenseapps.filepicker.AbstractFilePickerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

const val ARG_FEED_ID = "feed_id"
const val ARG_FEED_TITLE = "feed_title"
const val ARG_FEED_URL = "feed_url"
const val ARG_FEED_TAG = "feed_tag"

class FeedFragment : CoroutineScopedFragment() {

    private var adapter: FeedItemPagedListAdapter? = null
    private var recyclerView: RecyclerView? = null
    internal var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var emptyView: View? = null
    private var emptyAddFeed: View? = null
    private var emptyOpenFeeds: View? = null

    private val syncReceiver: BroadcastReceiver

    private var id: Long = ID_UNSET
    private var title: String? = ""
    private var url: String? = ""
    private var feedTag: String? = ""
    private var firstFeedLoad: Boolean = true
    private var displayTitle = ""
    private var customTitle = ""
    private var layoutManager: LinearLayoutManager? = null
    private var notify = false

    var feedViewModel: FeedViewModel? = null
    var feedItemsViewModel: FeedItemsViewModel? = null

    init {
        // Listens on sync broadcasts
        syncReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (SYNC_BROADCAST == intent.action) {
                    onSyncBroadcast(intent.getBooleanExtra(SYNC_BROADCAST_IS_ACTIVE, false))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arguments ->
            id = arguments.getLong(ARG_FEED_ID, ID_UNSET)
            title = arguments.getString(ARG_FEED_TITLE)
            url = arguments.getString(ARG_FEED_URL)
            feedTag = arguments.getString(ARG_FEED_TAG)

            arguments.getLongArray(EXTRA_FEEDITEMS_TO_MARK_AS_NOTIFIED)?.let {
                context?.let { context ->
                    val db = AppDatabase.getInstance(context)
                    launch(Dispatchers.Default) {
                        db.feedItemDao().markAsNotified(it.toList())
                    }
                }
            }
        }

        if (id == ID_UNSET && feedTag?.isNotEmpty() != true) {
            context?.let { context ->
                if (id == ID_UNSET) {
                    id = PrefUtils.getLastOpenFeedId(context)
                }
                if (feedTag?.isNotEmpty() != true) {
                    feedTag = PrefUtils.getLastOpenFeedTag(context)
                }
            }
        }

        when {
            id == ID_UNSET && feedTag?.isNotEmpty() == true -> {
                Log.d("JONAS", "Yup TAG")
                title = feedTag
            }
            id == ID_UNSET -> {
                id = ID_ALL_FEEDS
                title = getString(R.string.all_feeds)
            }
            id == ID_ALL_FEEDS -> {
                title = getString(R.string.all_feeds)
            }
        }
        Log.d("JONAS", "feedFragment create id $id")

        // Set this to equal title in case it's not a feed
        displayTitle = title ?: ""

        setHasOptionsMenu(true)

        // Remember choice in future
        val appContext = context?.applicationContext
        launch(Dispatchers.Default) {
            if (appContext != null) {
                PrefUtils.setLastOpenFeed(appContext, id, feedTag)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d("JONAS", "feedFragment createView id $id")
        val rootView = inflater.inflate(R.layout.fragment_feed, container, false)
        recyclerView = rootView.findViewById<View>(android.R.id.list) as RecyclerView

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recyclerView?.setHasFixedSize(true)

        if (TabletUtils.isTablet(activity)) {
            val cols = TabletUtils.numberOfFeedColumns(activity)
            // use a grid layout
            layoutManager = GridLayoutManager(activity,
                    cols)

            // TODO, use better dividers such as simple padding
            // I want some dividers
            recyclerView!!.addItemDecoration(DividerColor(activity, DividerColor.VERTICAL_LIST, 0, cols))
            // I want some dividers
            recyclerView!!.addItemDecoration(DividerColor(activity, DividerColor.HORIZONTAL_LIST))
        } else {
            // use a linear layout manager
            layoutManager = LinearLayoutManager(activity)
        }
        recyclerView?.layoutManager = layoutManager

        // Setup swipe refresh
        swipeRefreshLayout = rootView.findViewById<View>(R.id.swiperefresh) as SwipeRefreshLayout

        // The arrow will cycle between these colors (in order)
        swipeRefreshLayout!!.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3)

        swipeRefreshLayout!!.setOnRefreshListener {
            // Sync this specific feed(s) immediately
            requestFeedSync(
                    feedId = id,
                    feedTag = feedTag ?: "",
                    ignoreConnectivitySettings = true,
                    parallell = true,
                    forceNetwork = true
            )
        }

        // Set up the empty view
        emptyView = rootView.findViewById(android.R.id.empty)
        emptyAddFeed = emptyView?.findViewById(R.id.empty_add_feed)
        @Suppress("DEPRECATION")
        (emptyAddFeed as TextView).text = android.text.Html.fromHtml(getString(R.string.empty_feed_add))
        emptyOpenFeeds = emptyView?.findViewById(R.id.empty_open_feeds)
        @Suppress("DEPRECATION")
        (emptyOpenFeeds as TextView).text = android.text.Html.fromHtml(getString(R.string.empty_feed_open))

        emptyAddFeed?.setOnClickListener {
            startActivity(Intent(activity,
                    EditFeedActivity::class.java))
        }

        emptyOpenFeeds?.setOnClickListener { (activity as FeedActivity).openNavDrawer() }

        // specify an adapter
        adapter = FeedItemPagedListAdapter(activity!!, object : ActionCallback {
            override fun coroutineScope(): CoroutineScope {
                return this@FeedFragment
            }

            override fun onDismiss(item: PreviewItem?) {
                item?.let {
                    feedItemsViewModel?.toggleReadState(it)
                }
            }

            override fun onSwipeStarted() {
                // SwipeRefreshLayout does not honor requestDisallowInterceptTouchEvent
                swipeRefreshLayout?.isEnabled = false
            }

            override fun onSwipeCancelled() {
                // SwipeRefreshLayout does not honor requestDisallowInterceptTouchEvent
                swipeRefreshLayout?.isEnabled = true
            }

        })
        recyclerView?.adapter = adapter

        // Load some RSS
        val onlyUnread = PrefUtils.isShowOnlyUnread(activity!!)
        feedItemsViewModel = getFeedItemsViewModel(feedId = id, tag = feedTag
                ?: "", onlyUnread = onlyUnread)

        feedItemsViewModel?.liveDbPreviews?.observe(this, Observer<PagedList<PreviewItem>> {
            adapter?.submitList(it)
            emptyView?.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        })

        when {
            id > ID_UNSET -> { // Load feed if feed
                feedViewModel = getFeedViewModel(id)
                feedViewModel?.liveFeed?.observe(this, Observer {
                    Log.d("JONAS", "Feed $displayTitle")
                    it?.let { feed ->
                        this.title = feed.title
                        this.customTitle = feed.customTitle
                        this.displayTitle = feed.displayTitle
                        this.url = feed.url.toString()
                        this.notify = feed.notify
                        this.feedTag = feed.tag

                        (activity as AppCompatActivity?)?.supportActionBar?.title = displayTitle

                        // Update state of notification toggle
                        activity?.invalidateOptionsMenu()

                        // If user edits the feed then the variables and the UI should reflect it but we shouldn't add
                        // extra statistics on opening the feed.
                        if (firstFeedLoad) {
                            // Title has been fetched, so add shortcut
                            activity?.addDynamicShortcutToFeed(feed.displayTitle, feed.id, null)
                            // Report shortcut usage
                            activity?.reportShortcutToFeedUsed(feed.id)
                        }
                        firstFeedLoad = false
                    }
                })
            }
            id == ID_UNSET -> { // Load notification settings for tag
                Log.d("JONAS", "Tag $displayTitle")
                (activity as AppCompatActivity?)?.supportActionBar?.title = displayTitle

                activity?.let { activity ->
                    feedTag?.let { feedTag ->
                        AppDatabase.getInstance(activity).feedDao().loadLiveFeedsNotify(tag = feedTag).observe(this, Observer {
                            it.fold(true) { a, b -> a && b }
                                    .let { notify ->
                                        this.notify = notify
                                        // Update state of notification toggle
                                        activity.invalidateOptionsMenu()
                                    }
                        })
                    }
                }
            }
            else -> { // Load notification settings for all
                Log.d("JONAS", "Else $displayTitle")
                (activity as AppCompatActivity?)?.supportActionBar?.title = displayTitle

                activity?.let { activity ->
                    AppDatabase.getInstance(activity).feedDao().loadLiveFeedsNotify().observe(this, Observer {
                        it.fold(true) { a, b -> a && b }
                                .let { notify ->
                                    this.notify = notify
                                    // Update state of notification toggle
                                    activity.invalidateOptionsMenu()
                                }
                    })
                }
            }
        }

        return rootView
    }


    private fun onSyncBroadcast(syncing: Boolean) {
        // Background syncs trigger the sync layout
        if (swipeRefreshLayout!!.isRefreshing != syncing) {
            swipeRefreshLayout!!.isRefreshing = syncing
        }
    }

    override fun onActivityCreated(bundle: Bundle?) {
        super.onActivityCreated(bundle)

        // check all button
        (activity as FeedActivity).fabOnClickListener = {
            markAsRead()
        }
    }

    override fun onResume() {
        super.onResume()
        // Listen on broadcasts
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(activity!!).registerReceiver(syncReceiver,
                IntentFilter(SYNC_BROADCAST))
    }

    override fun onPause() {
        // Unregister receiver
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(syncReceiver)
        swipeRefreshLayout!!.isRefreshing = false
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.feed, menu)

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        if (id < 1) {
            menu?.findItem(R.id.action_edit_feed)?.isVisible = false
            menu?.findItem(R.id.action_delete_feed)?.isVisible = false
            menu?.findItem(R.id.action_add_templated)?.isVisible = false
        }

        // Set toggleable state
        menu?.findItem(R.id.action_only_unread)?.let { menuItem ->
            val onlyUnread = PrefUtils.isShowOnlyUnread(activity!!)
            menuItem.isChecked = onlyUnread
            menuItem.setTitle(if (onlyUnread) R.string.show_all_items else R.string.show_unread_items)

            menuItem.setIcon(
                    when (onlyUnread) {
                        true -> R.drawable.ic_visibility_off_white_24dp
                        false -> R.drawable.ic_visibility_white_24dp
                    }
            )
        }

        menu?.findItem(R.id.action_notify)?.let { menuItem ->
            setNotifyMenuItemState(menuItem)
        }

        super.onPrepareOptionsMenu(menu)
    }

    private fun setNotifyMenuItemState(menuItem: MenuItem) {
        menuItem.isChecked = notify
        if (notify) {
            menuItem.setIcon(R.drawable.ic_notifications_on_white_24dp)
        } else {
            menuItem.setIcon(R.drawable.ic_notifications_off_white_24dp)
        }

        menuItem.setTitle(if (notify) R.string.dont_notify_for_new_items else R.string.notify_for_new_items)
    }

    private fun setNotifications(on: Boolean) {
        val feedId = this.id
        val feedTag = this.feedTag
        val appContext = context?.applicationContext
        if (appContext != null) {
            launch(Dispatchers.Default) {
                // Set as notified so we don't spam
                feedItemsViewModel?.markAsNotified()
                val dao = AppDatabase.getInstance(appContext).feedDao()
                when {
                    feedId > ID_UNSET -> dao.setNotify(feedId, on)
                    feedId == ID_ALL_FEEDS -> dao.setAllNotify(on)
                    feedTag?.isNotEmpty() == true -> dao.setNotify(feedTag, on)
                }
            }
        }
    }

    /**
     * Mark all items as read in the list
     */
    private fun markAsRead() {
        // Cancel any notifications
        context?.applicationContext?.let { appContext ->
            feedItemsViewModel?.liveDbPreviews?.value?.forEach {
                // Can be null in case of placeholder values
                it?.id?.let { id ->
                    launch(Dispatchers.Default) {
                        cancelNotification(appContext, id)
                    }
                }
            }
        }
        // Then mark as read
        feedItemsViewModel?.markAllAsRead()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val id = menuItem.itemId
        return when {
            id == R.id.action_sync -> {
                // Sync all feeds when menu button pressed
                requestFeedSync(
                        parallell = true,
                        ignoreConnectivitySettings = true,
                        forceNetwork = true
                )
                true
            }
            id == R.id.action_edit_feed && this.id > ID_UNSET -> {
                this.id.let { feedId ->
                    val i = Intent(activity, EditFeedActivity::class.java)
                    // TODO do not animate the back movement here
                    i.putExtra(ARG_ID, feedId)
                    i.putExtra(ARG_CUSTOMTITLE, customTitle)
                    i.putExtra(ARG_TITLE, title)
                    i.putExtra(ARG_FEED_TAG, feedTag)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }

                true
            }
            id == R.id.action_add_templated && this.id > ID_UNSET -> {
                val i = Intent(activity, EditFeedActivity::class.java)
                // TODO do not animate the back movement here
                i.putExtra(TEMPLATE, true)
                i.putExtra(ARG_FEED_TAG, feedTag)
                i.data = Uri.parse(url)
                startActivity(i)
                true
            }
            id == R.id.action_delete_feed && this.id > ID_UNSET -> {
                val feedId = this.id
                val appContext = activity?.applicationContext
                if (appContext != null) {
                    launch(Dispatchers.Default) {
                        feedViewModel?.deleteFeed()

                        // Remove from shortcuts
                        appContext.removeDynamicShortcutToFeed(feedId)
                    }
                }

                // Tell activity to open another fragment
                findNavController().navigate(R.id.action_feedFragment_self, bundle {
                    putLong(ARG_FEED_ID, ID_ALL_FEEDS)
                })
                true
            }
            id == R.id.action_only_unread -> {
                val onlyUnread = !menuItem.isChecked
                PrefUtils.setPrefShowOnlyUnread(activity!!, onlyUnread)
                menuItem.isChecked = onlyUnread
                if (onlyUnread) {
                    menuItem.setIcon(R.drawable.ic_visibility_off_white_24dp)
                } else {
                    menuItem.setIcon(R.drawable.ic_visibility_white_24dp)
                }

                menuItem.setTitle(if (onlyUnread) R.string.show_unread_items else R.string.show_all_items)

                feedItemsViewModel?.setOnlyUnread(onlyUnread)

                true
            }
            id == R.id.action_notify -> {
                notify = !menuItem.isChecked

                setNotifyMenuItemState(menuItem)
                setNotifications(notify)
                true
            }
            id == R.id.action_add -> {
                startActivityForResult(Intent(context, EditFeedActivity::class.java), EDIT_FEED_CODE)
                true
            }
            id == R.id.action_opml_export -> {
                // Choose file, then export
                val intent: Intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.type = "text/opml"
                    intent.putExtra(Intent.EXTRA_TITLE, "feeder.opml")
                } else {
                    intent = Intent(context, MyFilePickerActivity::class.java)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_MODE, AbstractFilePickerActivity.MODE_NEW_FILE)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_ALLOW_EXISTING_FILE, true)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_START_PATH,
                            File(Environment.getExternalStorageDirectory(), "feeder.opml").path)
                }
                startActivityForResult(intent, EXPORT_OPML_CODE)
                true
            }
            id == R.id.action_opml_import -> {
                // Choose file
                val intent: Intent
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "*/*"
                    intent.putExtra(Intent.EXTRA_MIME_TYPES,
                            arrayOf("text/plain", "text/xml", "text/opml", "*/*"))
                } else {
                    intent = Intent(context, MyFilePickerActivity::class.java)
                    intent.putExtra(AbstractFilePickerActivity.EXTRA_SINGLE_CLICK, true)
                }
                startActivityForResult(intent, IMPORT_OPML_CODE)
                true
            }
            id == R.id.action_settings -> {
                findNavController().navigate(R.id.action_feedFragment_to_settingsFragment)
                true
            }
            id == R.id.action_reportbug -> {
                try {
                    startActivity(emailBugReportIntent())
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.no_email_client, Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            EXPORT_OPML_CODE -> {
                val uri: Uri? = data?.data
                if (uri != null) {
                    val appContext = context!!.applicationContext
                    launch(Dispatchers.Default) {
                        exportOpml(appContext, uri)
                    }
                }
            }
            IMPORT_OPML_CODE -> {
                val uri: Uri? = data?.data
                if (uri != null) {
                    val appContext = context!!.applicationContext
                    launch(Dispatchers.Default) {
                        importOpml(appContext, uri)
                    }
                }
            }
            EDIT_FEED_CODE -> {
                data?.data?.lastPathSegment?.toLong()?.let { id ->
                    findNavController().navigate(R.id.action_feedFragment_self, bundle {
                        putLong(ARG_FEED_ID, id)
                        putString(ARG_FEED_TAG, data.extras?.getString(ARG_FEED_TAG))
                    })
                }
            }
        }
    }

    companion object {

        /**
         * Returns a new instance of this fragment
         */
        fun newInstance(id: Long, title: String?, url: String?,
                        tag: String?): FeedFragment {
            val fragment = FeedFragment()
            fragment.arguments = bundle {
                setLong(ARG_FEED_ID to id)
                setString(ARG_FEED_TITLE to title)
                setString(ARG_FEED_URL to url)
                setString(ARG_FEED_TAG to tag)
            }
            return fragment
        }
    }
}

fun Fragment.setHideableToolbar(hideable: Boolean) {
    activity?.let { activity ->
        if (activity is FeedActivity) {
            if (hideable) {
                activity.hideableToolbar()
            } else {
                activity.fixedToolbar()
            }
        }
    }
}
