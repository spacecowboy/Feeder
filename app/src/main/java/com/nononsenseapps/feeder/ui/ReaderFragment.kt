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


import android.content.ActivityNotFoundException
import android.content.Intent
import android.database.Cursor
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ShareActionProvider
import android.text.Spanned
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.Background
import com.nononsenseapps.feeder.db.COL_ID
import com.nononsenseapps.feeder.db.FEED_ITEM_FIELDS
import com.nononsenseapps.feeder.db.FeedItemSQL
import com.nononsenseapps.feeder.db.URI_FEEDITEMS
import com.nononsenseapps.feeder.db.asFeedItem
import com.nononsenseapps.feeder.model.cancelNotificationInBackground
import com.nononsenseapps.feeder.ui.text.ImageTextLoader
import com.nononsenseapps.feeder.ui.text.toSpannedWithNoImages
import com.nononsenseapps.feeder.util.FileLog
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.TabletUtils
import com.nononsenseapps.feeder.util.asFeedItem
import com.nononsenseapps.feeder.util.firstOrNull
import com.nononsenseapps.feeder.util.markItemAsReadAndNotified
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

class ReaderFragment : Fragment(), LoaderManager.LoaderCallbacks<Any?> {

    private val dateTimeFormat = DateTimeFormat.mediumDate().withLocale(Locale.getDefault())

    private var _id: Long = -1
    // All content contained in RssItem
    private var rssItem: FeedItemSQL? = null
    private var bodyTextView: TextView? = null
    private var scrollView: ObservableScrollView? = null
    private var titleTextView: TextView? = null

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
                launch(Background) {
                    it.contentResolver.markItemAsReadAndNotified(itemId)
                    cancelNotificationInBackground(it, itemId)
                }
            }
            loaderManager.restartLoader(ITEM_LOADER, Bundle(), this)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val theLayout: Int
        if (TabletUtils.isTablet(activity)) {
            theLayout = R.layout.fragment_reader_tablet
        } else {
            theLayout = R.layout.fragment_reader
        }
        val rootView = inflater.inflate(theLayout, container, false)

        scrollView = rootView.findViewById<View>(R.id.scroll_view) as ObservableScrollView
        titleTextView = rootView.findViewById<View>(R.id.story_title) as TextView
        bodyTextView = rootView.findViewById<View>(R.id.story_body) as TextView
        val mAuthorTextView = rootView.findViewById<View>(R.id.story_author) as TextView
        val mFeedTitleTextView = rootView.findViewById<View>(R.id
                .story_feedtitle) as TextView

        setViewTitle()

        mFeedTitleTextView.text = rssItem!!.feedtitle

        if (rssItem!!.author == null && rssItem!!.pubDate != null) {
            mAuthorTextView.text = getString(R.string.on_date,
                    rssItem!!.pubDate!!.withZone(DateTimeZone.getDefault())
                            .toString(dateTimeFormat))
        } else if (rssItem!!.pubDate != null) {
            mAuthorTextView.text = getString(R.string.by_author_on_date,
                    rssItem!!.author,
                    rssItem!!.pubDate!!.withZone(DateTimeZone.getDefault())
                            .toString(dateTimeFormat))
        } else {
            mAuthorTextView.visibility = View.GONE
        }

        setViewBody()

        return rootView
    }

    private fun setViewTitle() {
        if (rssItem!!.title.isEmpty()) {
            titleTextView!!.text = rssItem!!.plaintitle
        } else {
            titleTextView!!.text = toSpannedWithNoImages(activity!!, rssItem!!.title, rssItem!!.feedUrl)
        }
    }

    private fun setViewBody() {
        if (!rssItem!!.description.isEmpty()) {
            // Set without images as a place holder
            bodyTextView!!.text = toSpannedWithNoImages(activity!!, rssItem!!.description, rssItem!!.feedUrl)

            // Load images in text
            loaderManager.restartLoader(TEXT_LOADER, Bundle(), this)
        }
    }

    override fun onActivityCreated(bundle: Bundle?) {
        super.onActivityCreated(bundle)
        scrollView?.let {
            (activity as BaseActivity).enableActionBarAutoHide(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        rssItem?.storeInBundle(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDetach() {
        loaderManager.destroyLoader(TEXT_LOADER)
        super.onDetach()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.reader, menu)

        // Locate MenuItem with ShareActionProvider
        val shareItem = menu!!.findItem(R.id.action_share)

        // Fetch and store ShareActionProvider
        val shareActionProvider = MenuItemCompat.getActionProvider(shareItem) as ShareActionProvider

        // Set intent
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, rssItem!!.link)
        shareActionProvider.setShareIntent(shareIntent)

        // Show/Hide enclosure
        menu.findItem(R.id.action_open_enclosure).isVisible = rssItem!!.enclosurelink != null
        // Add filename to tooltip
        if (rssItem!!.enclosurelink != null) {
            val filename = rssItem!!.enclosureFilename
            if (filename != null) {
                menu.findItem(R.id.action_open_enclosure).title = filename
            }
        }

        // Don't forget super call here
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem?): Boolean {
        val uri: Uri
        when (menuItem!!.itemId) {
            R.id.action_open_in_browser -> {
                uri = Uri.parse(rssItem!!.link)
                if (uri.isRelative) {
                    Toast.makeText(activity, "Sorry, can't handle relative links yet.", Toast.LENGTH_SHORT).show()
                }
                // Open in browser
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(activity, "Couldn't find an activity to open that link with", Toast.LENGTH_SHORT).show()
                    FileLog.getInstance(activity!!).d("No such activity: " + e)
                }

                return true
            }
            R.id.action_open_enclosure -> {
                uri = Uri.parse(rssItem!!.enclosurelink)
                if (uri.isRelative) {
                    Toast.makeText(activity, R.string.no_activity_for_link, Toast.LENGTH_SHORT).show()
                }
                // Open enclosure link
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(activity, R.string.no_activity_for_link, Toast.LENGTH_SHORT).show()
                    FileLog.getInstance(activity!!).d("No such activity: " + e)
                }

                return true
            }
            else -> return super.onOptionsItemSelected(menuItem)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Any?>? = when (id) {
        ITEM_LOADER -> {
            val cl = CursorLoader(context!!, URI_FEEDITEMS,
                    FEED_ITEM_FIELDS,
                    COL_ID + " IS ?",
                    arrayOf(java.lang.Long.toString(rssItem!!.id)), null)
            cl.setUpdateThrottle(100)
            cl as Loader<Any?>
        }
        TEXT_LOADER -> {
            ImageTextLoader(activity as FragmentActivity, rssItem!!.description, rssItem?.feedUrl
                    ?: sloppyLinkToStrictURL(""),
                    maxImageSize(), PrefUtils.shouldLoadImages(activity!!)) as Loader<Any?>
        }
        else -> null
    }

    private fun maxImageSize(): Point {
        val size = Point()
        activity?.let{
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

    override fun onLoadFinished(loader: Loader<Any?>?,
                                data: Any?) {
        if (loader?.id == ITEM_LOADER) {
            val cursor = data as Cursor?
            cursor?.use { c ->
                c.firstOrNull()?.asFeedItem()?.let {
                    rssItem = it
                    setViewTitle()
                    setViewBody()
                }
            }
            loader.cancelLoad()
        } else if (loader?.id == TEXT_LOADER) {
            if (data != null) {
                bodyTextView?.text = data as Spanned?
            }
            loader.cancelLoad()
        }
    }

    override fun onLoaderReset(loader: Loader<Any?>?) {
        // nothing really
    }

    companion object {

        fun newInstance(rssItem: FeedItemSQL): ReaderFragment {
            val fragment = ReaderFragment()
            // Save some time on load
            fragment.rssItem = rssItem
            fragment._id = rssItem.id

            fragment.arguments = rssItem.asBundle()
            return fragment
        }
    }
}
