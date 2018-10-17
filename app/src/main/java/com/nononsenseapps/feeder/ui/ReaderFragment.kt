package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.BackgroundUI
import com.nononsenseapps.feeder.db.room.AppDatabase
import com.nononsenseapps.feeder.db.room.FeedItemWithFeed
import com.nononsenseapps.feeder.db.room.ID_UNSET
import com.nononsenseapps.feeder.model.cancelNotificationInBackground
import com.nononsenseapps.feeder.model.getFeedItemViewModel
import com.nononsenseapps.feeder.ui.text.toSpannedWithNoImages
import com.nononsenseapps.feeder.util.TabletUtils
import com.nononsenseapps.feeder.util.asFeedItemFoo
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.views.ObservableScrollView
import kotlinx.coroutines.experimental.launch
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*

const val ARG_TITLE = "title"
const val ARG_CUSTOMTITLE = "customtitle"
const val ARG_DESCRIPTION = "body"
const val ARG_LINK = "link"
const val ARG_ENCLOSURE = "enclosure"
const val ARG_IMAGEURL = "imageUrl"
const val ARG_ID = "dbid"
const val ARG_FEEDTITLE = "feedtitle"
const val ARG_AUTHOR = "author"
const val ARG_DATE = "date"

class ReaderFragment : androidx.fragment.app.Fragment() {

    private val dateTimeFormat = DateTimeFormat.mediumDate().withLocale(Locale.getDefault())

    private var _id: Long = ID_UNSET
    // All content contained in RssItem
    private var rssItem: FeedItemWithFeed? = null
    private lateinit var bodyTextView: TextView
    private lateinit var scrollView: ObservableScrollView
    private lateinit var titleTextView: TextView
    private lateinit var mAuthorTextView: TextView
    private lateinit var mFeedTitleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            _id = savedInstanceState.getLong(ARG_ID)
            rssItem = savedInstanceState.asFeedItemFoo()

        } else if (rssItem == null && arguments != null) {
            // Construct from arguments
            arguments?.let { arguments ->
                _id = arguments.getLong(ARG_ID, ID_UNSET)
                rssItem = arguments.asFeedItemFoo()
            }
        }

        if (_id > ID_UNSET) {
            val itemId = _id
            val appContext = context?.applicationContext
            appContext?.let {
                val db = AppDatabase.getInstance(appContext)
                launch(BackgroundUI) {
                    db.feedItemDao().markAsReadAndNotified(itemId)
                    cancelNotificationInBackground(it, itemId)
                }
            }
        }

        setHasOptionsMenu(true)

        val viewModel = getFeedItemViewModel(_id)
        viewModel.liveItem.observe(this, androidx.lifecycle.Observer {
            rssItem = it

            rssItem?.let { rssItem ->
                setViewTitle()

                mFeedTitleTextView.text = rssItem.feedTitle

                if (rssItem.author == null && rssItem.pubDate != null) {
                    rssItem.pubDate?.let { pubDate ->
                        mAuthorTextView.text = getString(R.string.on_date,
                                pubDate.withZone(DateTimeZone.getDefault())
                                        .toString(dateTimeFormat))
                    }
                } else if (rssItem.pubDate != null) {
                    rssItem.pubDate?.let { pubDate ->
                        mAuthorTextView.text = getString(R.string.by_author_on_date,
                                rssItem.author,
                                pubDate.withZone(DateTimeZone.getDefault())
                                        .toString(dateTimeFormat))
                    }
                } else {
                    mAuthorTextView.visibility = View.GONE
                }
            }
        })

        viewModel.liveImageText.observe(this, androidx.lifecycle.Observer {
            bodyTextView.text = it
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val theLayout = if (TabletUtils.isTablet(activity)) {
            R.layout.fragment_reader_tablet
        } else {
            R.layout.fragment_reader
        }
        val rootView = inflater.inflate(theLayout, container, false)

        scrollView = rootView.findViewById<View>(R.id.scroll_view) as ObservableScrollView
        titleTextView = rootView.findViewById<View>(R.id.story_title) as TextView
        bodyTextView = rootView.findViewById<View>(R.id.story_body) as TextView
        mAuthorTextView = rootView.findViewById<View>(R.id.story_author) as TextView
        mFeedTitleTextView = rootView.findViewById<View>(R.id
                .story_feedtitle) as TextView

        return rootView
    }

    private fun setViewTitle() {
        rssItem?.let { rssItem ->
            if (rssItem.title.isEmpty()) {
                titleTextView.text = rssItem.plainTitle
            } else {
                titleTextView.text = toSpannedWithNoImages(activity!!, rssItem.title, rssItem.feedUrl)
            }
        }
    }

    override fun onActivityCreated(bundle: Bundle?) {
        super.onActivityCreated(bundle)
        scrollView.let {
            (activity as BaseActivity).enableActionBarAutoHide(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        rssItem?.storeInBundle(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.reader, menu)

        // Locate MenuItem with ShareActionProvider
        val shareItem = menu!!.findItem(R.id.action_share)

        // Fetch and store ShareActionProvider
        val shareActionProvider = MenuItemCompat.getActionProvider(shareItem) as ShareActionProvider

        // Set intent
        rssItem?.let { rssItem ->

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, rssItem.link)
            shareActionProvider.setShareIntent(shareIntent)

            // Show/Hide enclosure
            menu.findItem(R.id.action_open_enclosure).isVisible = rssItem.enclosureLink != null
            // Add filename to tooltip
            if (rssItem.enclosureLink != null) {
                val filename = rssItem.enclosureFilename
                if (filename != null) {
                    menu.findItem(R.id.action_open_enclosure).title = filename
                }

            }
        }

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        return when (menuItem!!.itemId) {
            R.id.action_open_in_webview -> {
                // Open in web view
                rssItem?.let { rssItem ->
                    rssItem.link?.let { link ->
                        context?.let { context ->
                            val intent = Intent(context, ReaderWebViewActivity::class.java)
                            intent.putExtra(ARG_URL, link)
                            intent.putExtra(ARG_ENCLOSURE, rssItem.enclosureLink)
                            startActivity(intent)
                            activity?.finish()
                        }
                    }
                }
                true
            }
            R.id.action_open_in_browser -> {
                val link = rssItem?.link
                if (link != null) {
                    context?.let { context ->
                        openLinkInBrowser(context, link)
                    }
                }

                true
            }
            R.id.action_open_enclosure -> {
                val link = rssItem?.enclosureLink
                if (link != null) {
                    context?.let { context ->
                        openLinkInBrowser(context, link)
                    }
                }

                true
            }
            R.id.action_mark_as_unread -> {
                context?.applicationContext?.let {
                    val db = AppDatabase.getInstance(it)
                    launch(BackgroundUI) {
                        db.feedItemDao().markAsRead(_id, unread = true)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }
}
