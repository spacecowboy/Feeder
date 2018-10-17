package com.nononsenseapps.feeder.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.Background
import com.nononsenseapps.feeder.coroutines.BackgroundUI
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.ID_ALL_FEEDS
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.FeedItemsViewModel
import com.nononsenseapps.feeder.model.FeedViewModel
import com.nononsenseapps.feeder.model.PreviewItem
import com.nononsenseapps.feeder.model.SYNC_BROADCAST
import com.nononsenseapps.feeder.model.SYNC_BROADCAST_IS_ACTIVE
import com.nononsenseapps.feeder.model.cancelNotification
import com.nononsenseapps.feeder.model.getFeedItemsViewModel
import com.nononsenseapps.feeder.model.getFeedViewModel
import com.nononsenseapps.feeder.model.requestFeedSync
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.TabletUtils
import com.nononsenseapps.feeder.util.addDynamicShortcutToFeed
import com.nononsenseapps.feeder.util.bundle
import com.nononsenseapps.feeder.util.removeDynamicShortcutToFeed
import com.nononsenseapps.feeder.util.reportShortcutToFeedUsed
import com.nononsenseapps.feeder.util.setLong
import com.nononsenseapps.feeder.util.setString
import kotlinx.coroutines.experimental.launch

const val ARG_FEED_ID = "feed_id"
const val ARG_FEED_TITLE = "feed_title"
const val ARG_FEED_URL = "feed_url"
const val ARG_FEED_TAG = "feed_tag"

class FeedFragment : Fragment() {

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
    private var customTitle = ""
    private var layoutManager: LinearLayoutManager? = null
    private var checkAllButton: View? = null
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

            // It's a feedTag, use as title
            if (id == ID_UNSET) {
                title = feedTag
            }

            // Special feedTag
            if (id == ID_ALL_FEEDS) {
                title = getString(R.string.all_feeds)
            }
        }

        setHasOptionsMenu(true)

        // Load some RSS
        val onlyUnread = PrefUtils.isShowOnlyUnread(activity!!)
        feedItemsViewModel = getFeedItemsViewModel(feedId = id, tag = feedTag
                ?: "", onlyUnread = onlyUnread)

        feedItemsViewModel?.liveDbPreviews?.observe(this, Observer {
            adapter?.submitList(it)
            emptyView?.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        })

        when {
            id > ID_UNSET -> { // Load feed if feed
                feedViewModel = getFeedViewModel(id)
                feedViewModel?.liveFeed?.observe(this, Observer {
                    it?.let { feed ->
                        this.title = feed.title
                        this.customTitle = feed.customTitle
                        this.url = feed.url.toString()
                        this.notify = feed.notify
                        this.feedTag = feed.tag

                        (activity as BaseActivity).supportActionBar?.title = feed.displayTitle

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

        // Remember choice in future
        val appContext = context?.applicationContext
        launch(Background) {
            if (appContext != null) {
                PrefUtils.setLastOpenFeed(appContext, id, feedTag)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_feed, container, false)
        recyclerView = rootView.findViewById<View>(android.R.id.list) as RecyclerView

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recyclerView!!.setHasFixedSize(true)

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
        recyclerView!!.layoutManager = layoutManager

        // Setup swipe refresh
        swipeRefreshLayout = rootView.findViewById<View>(R.id.swiperefresh) as SwipeRefreshLayout

        // The arrow will cycle between these colors (in order)
        swipeRefreshLayout!!.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3)

        swipeRefreshLayout!!.setOnRefreshListener {
            // Sync this specific feed(s)
            requestFeedSync(id, feedTag ?: "")
        }

        // Set up the empty view
        emptyView = rootView.findViewById(android.R.id.empty)
        emptyAddFeed = emptyView!!.findViewById(R.id.empty_add_feed)
        @Suppress("DEPRECATION")
        (emptyAddFeed as TextView).text = android.text.Html.fromHtml(getString(R.string.empty_feed_add))
        emptyOpenFeeds = emptyView!!.findViewById(R.id.empty_open_feeds)
        @Suppress("DEPRECATION")
        (emptyOpenFeeds as TextView).text = android.text.Html.fromHtml(getString(R.string.empty_feed_open))

        emptyAddFeed!!.setOnClickListener {
            startActivity(Intent(activity,
                    EditFeedActivity::class.java))
        }

        emptyOpenFeeds!!.setOnClickListener { (activity as BaseActivity).openNavDrawer() }

        // specify an adapter
        adapter = FeedItemPagedListAdapter(activity!!, object : DismissedListener {
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
        recyclerView!!.adapter = adapter

        // check all button
        checkAllButton = rootView.findViewById(R.id.checkall_button)
        checkAllButton!!.setOnClickListener { markAsRead() }

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

        val ab = (activity as BaseActivity).supportActionBar
        ab?.title = title
        recyclerView?.let {
            (activity as BaseActivity).enableActionBarAutoHide(it)
        }
    }

    override fun onResume() {
        super.onResume()
        // List might be shorter than screen once item has been read
        (activity as BaseActivity).showActionBar()
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
        inflater!!.inflate(R.menu.feed_fragment, menu)

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
            menuItem.setTitle(if (onlyUnread) R.string.show_unread_items else R.string.show_all_items)

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
            launch(Background) {
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
            feedItemsViewModel?.liveDbPreviews?.value?.forEach{
                launch(Background) {
                    cancelNotification(appContext, it.id)
                }
            }
        }
        // Then mark as read
        feedItemsViewModel?.markAllAsRead()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val id = menuItem.itemId.toLong()
        return when {
            id == R.id.action_sync.toLong() -> {
                // Sync all feeds when menu button pressed
                requestFeedSync()
                true
            }
            id == R.id.action_edit_feed.toLong() && this.id > ID_UNSET -> {
                this.id.let { feedId ->
                    val i = Intent(activity, EditFeedActivity::class.java)
                    // TODO do not animate the back movement here
                    i.putExtra(SHOULD_FINISH_BACK, true)
                    i.putExtra(ARG_ID, feedId)
                    i.putExtra(ARG_CUSTOMTITLE, customTitle)
                    i.putExtra(ARG_TITLE, title)
                    i.putExtra(ARG_FEED_TAG, feedTag)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }

                true
            }
            id == R.id.action_add_templated.toLong() && this.id > ID_UNSET -> {
                val i = Intent(activity, EditFeedActivity::class.java)
                // TODO do not animate the back movement here
                i.putExtra(SHOULD_FINISH_BACK, true)
                i.putExtra(TEMPLATE, true)
                i.putExtra(ARG_FEED_TAG, feedTag)
                i.data = Uri.parse(url)
                startActivity(i)
                true
            }
            id == R.id.action_delete_feed.toLong() && this.id > ID_UNSET -> {
                val feedId = this.id
                val appContext = activity?.applicationContext
                if (appContext != null) {
                    launch(BackgroundUI) {
                        feedViewModel?.deleteFeed()

                        // Remove from shortcuts
                        appContext.removeDynamicShortcutToFeed(feedId)
                    }
                }

                // Tell activity to open another fragment
                (activity as FeedActivity).showAllFeeds(true)
                true
            }
            id == R.id.action_only_unread.toLong() -> {
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
            id == R.id.action_notify.toLong() -> {
                notify = !menuItem.isChecked

                setNotifyMenuItemState(menuItem)
                setNotifications(notify)
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
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
