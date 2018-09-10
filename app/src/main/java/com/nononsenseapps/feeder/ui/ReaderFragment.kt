/*
 * Copyright (c) 2015 Jonas Kalderstam.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.feeder.ui


import android.content.Intent
import android.database.Cursor
import android.graphics.Point
import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.BackgroundUI
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.FEED_ITEM_FIELDS
import com.nononsenseapps.feeder.db.FeedItemSQL
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.asFeedItem
import com.nononsenseapps.feeder.model.cancelNotificationInBackground
import com.nononsenseapps.feeder.ui.text.ImageTextLoader
import com.nononsenseapps.feeder.ui.text.toSpannedWithNoImages
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.TabletUtils
import com.nononsenseapps.feeder.util.asFeedItem
import com.nononsenseapps.feeder.util.firstOrNull
import com.nononsenseapps.feeder.util.markItemAsReadAndNotified
import com.nononsenseapps.feeder.util.markItemAsUnread
import com.nononsenseapps.feeder.util.openLinkInBrowser
import com.nononsenseapps.feeder.util.sloppyLinkToStrictURL
import com.nononsenseapps.feeder.views.ObservableScrollView
import kotlinx.coroutines.experimental.launch
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.util.*

const val ARG_TITLE = "title"
const val ARG_DESCRIPTION = "body"
const val ARG_LINK = "link"
const val ARG_ENCLOSURE = "enclosure"
const val ARG_IMAGEURL = "imageurl"
const val ARG_ID = "dbid"
const val ARG_FEEDTITLE = "feedtitle"
const val ARG_AUTHOR = "author"
const val ARG_DATE = "date"

private const val TEXT_LOADER = 1
private const val ITEM_LOADER = 2

class ReaderFragment : Fragment(), LoaderCallbacks<Any?> {

    private val dateTimeFormat = DateTimeFormat.mediumDate().withLocale(Locale.getDefault())

    private var _id: Long = -1
    // All content contained in RssItem
    private var rssItem: FeedItemSQL? = null
    private lateinit var bodyTextView: TextView
    private lateinit var scrollView: ObservableScrollView
    private lateinit var titleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            _id = savedInstanceState.getLong(ARG_ID)
            rssItem = savedInstanceState.asFeedItem()

        } else if (rssItem == null && arguments != null) {
            // Construct from arguments
            _id = arguments!!.getLong(ARG_ID, -1)
            rssItem = arguments!!.asFeedItem()
        }

        if (_id > 0) {
            val itemId = _id
            val appContext = context?.applicationContext
            appContext?.let {
                launch(BackgroundUI) {
                    it.contentResolver.markItemAsReadAndNotified(itemId)
                    cancelNotificationInBackground(it, itemId)
                }
            }
            LoaderManager.getInstance(this).restartLoader(ITEM_LOADER, Bundle(), this)
        }

        setHasOptionsMenu(true)
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
        val mAuthorTextView = rootView.findViewById<View>(R.id.story_author) as TextView
        val mFeedTitleTextView = rootView.findViewById<View>(R.id
                .story_feedtitle) as TextView

        rssItem?.let { rssItem ->
            setViewTitle()

            mFeedTitleTextView.text = rssItem.feedtitle

            if (rssItem.author == null && rssItem.pubDate != null) {
                mAuthorTextView.text = getString(R.string.on_date,
                        rssItem.pubDate.withZone(DateTimeZone.getDefault())
                                .toString(dateTimeFormat))
            } else if (rssItem.pubDate != null) {
                mAuthorTextView.text = getString(R.string.by_author_on_date,
                        rssItem.author,
                        rssItem.pubDate.withZone(DateTimeZone.getDefault())
                                .toString(dateTimeFormat))
            } else {
                mAuthorTextView.visibility = View.GONE
            }

            setViewBody()
        }

        return rootView
    }

    private fun setViewTitle() {
        rssItem?.let { rssItem ->
            if (rssItem.title.isEmpty()) {
                titleTextView.text = rssItem.plaintitle
            } else {
                titleTextView.text = toSpannedWithNoImages(activity!!, rssItem.title, rssItem.feedUrl)
            }
        }
    }

    private fun setViewBody() {
        rssItem?.let { rssItem ->
            if (!rssItem.description.isEmpty()) {
                // Set without images as a place holder
                bodyTextView.text = toSpannedWithNoImages(activity!!, rssItem.description, rssItem.feedUrl)

                // Load images in text
                LoaderManager.getInstance(this).restartLoader(TEXT_LOADER, Bundle(), this)
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

    override fun onDetach() {
        LoaderManager.getInstance(this).let {
            it.destroyLoader(TEXT_LOADER)
            it.destroyLoader(ITEM_LOADER)
        }
        super.onDetach()
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
            menu.findItem(R.id.action_open_enclosure).isVisible = rssItem.enclosurelink != null
            // Add filename to tooltip
            if (rssItem.enclosurelink != null) {
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
                            intent.putExtra(ARG_ENCLOSURE, rssItem.enclosurelink)
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
                val link = rssItem?.enclosurelink
                if (link != null) {
                    context?.let { context ->
                        openLinkInBrowser(context, link)
                    }
                }

                true
            }
            R.id.action_mark_as_unread -> {
                val appContext = context?.applicationContext
                launch(BackgroundUI) {
                    appContext?.contentResolver?.markItemAsUnread(_id)
                }
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateLoader(id: Int, args: Bundle?): androidx.loader.content.Loader<Any?> = when (id) {
        ITEM_LOADER -> {
            val cl = androidx.loader.content.CursorLoader(context!!, URI_FEEDITEMS,
                    FEED_ITEM_FIELDS,
                    "$COL_ID IS ?",
                    arrayOf(java.lang.Long.toString(rssItem!!.id)), null)
            cl.setUpdateThrottle(100)
            cl as androidx.loader.content.Loader<Any?>
        }
        // TEXT_LOADER
        else -> {
            ImageTextLoader(activity as androidx.fragment.app.FragmentActivity, rssItem!!.description, rssItem?.feedUrl
                    ?: sloppyLinkToStrictURL(""),
                    maxImageSize(), PrefUtils.shouldLoadImages(activity!!)) as androidx.loader.content.Loader<Any?>
        }
    }

    private fun maxImageSize(): Point {
        val size = Point()
        activity?.let {
            it.windowManager?.defaultDisplay?.getSize(size)
            if (TabletUtils.isTablet(it)) {
                // Using twice window height since we do scroll vertically
                size.set(Math.round(resources.getDimension(R.dimen.reader_tablet_width)), 2 * size.y)
            } else {
                // Base it on window size
                size.set(size.x - 2 * Math.round(resources.getDimension(R.dimen.keyline_1)), 2 * size.y)
            }
        }
        return size
    }

    override fun onLoadFinished(loader: androidx.loader.content.Loader<Any?>,
                                data: Any?) {
        if (loader.id == ITEM_LOADER) {
            val cursor = data as Cursor?
            cursor?.use { c ->
                c.firstOrNull()?.asFeedItem()?.let {
                    rssItem = it
                    setViewTitle()
                    setViewBody()
                }
            }
        } else if (loader.id == TEXT_LOADER) {
            if (data != null) {
                bodyTextView.text = data as Spanned?
            }
        }
        LoaderManager.getInstance(this).destroyLoader(loader.id)
    }

    override fun onLoaderReset(loader: androidx.loader.content.Loader<Any?>) {
        // nothing really
    }
}
