package com.nononsenseapps.feeder.ui

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import com.nononsenseapps.feeder.R
import com.nononsenseapps.feeder.coroutines.Background
import com.nononsenseapps.feeder.db.FeedItemSQL
import com.nononsenseapps.feeder.model.cancelNotificationInBackground
import com.nononsenseapps.feeder.util.GlideUtils
import com.nononsenseapps.feeder.util.PrefUtils
import com.nononsenseapps.feeder.util.markItemAsRead
import com.nononsenseapps.feeder.util.markItemAsUnread
import kotlinx.coroutines.experimental.launch

// Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
class FeedItemHolder(val view: View, private val feedAdapter: FeedAdapter) :
        RecyclerView.ViewHolder(view), View.OnClickListener, ViewTreeObserver.OnPreDrawListener {
    private val TAG = "FeedItemHolder"
    private val titleTextView: TextView = view.findViewById<View>(R.id.story_snippet) as TextView
    val dateTextView: TextView = view.findViewById<View>(R.id.story_date) as TextView
    val authorTextView: TextView = view.findViewById<View>(R.id.story_author) as TextView
    val imageView: ImageView = view.findViewById<View>(R.id.story_image) as ImageView
    private val bgFrame: View = view.findViewById(R.id.swiping_item)
    private val checkLeft: View = view.findViewById(R.id.check_left)
    private val checkRight: View = view.findViewById(R.id.check_right)
    private val checkBg: View = view.findViewById(R.id.check_bg)

    var rssItem: FeedItemSQL? = null

    init {
        view.setOnClickListener(this)
        // Swipe handler
        view.setOnTouchListener(SwipeDismissTouchListener(view, null, object : SwipeDismissTouchListener.DismissCallbacks {
            override fun canDismiss(token: Any?): Boolean = rssItem != null

            override fun onDismiss(view: View, token: Any?) {
                rssItem!!.unread = !rssItem!!.unread
                // Update the item directly before updating database
                if (!PrefUtils.isShowOnlyUnread(feedAdapter.feedFragment.activity!!)) {
                    // Just update the view state
                    fillTitle()
                    resetView()
                } else {
                    // Remove it from the dataset directly
                    feedAdapter.items.remove(rssItem)
                }
                // Make database consistent with content
                val appContext = feedAdapter.feedFragment.context?.applicationContext
                val itemId = rssItem!!.id
                val unread = rssItem!!.unread
                if (appContext != null) {
                    launch(Background) {
                        when (unread) {
                            true -> appContext.contentResolver.markItemAsUnread(itemId)
                            false -> {
                                appContext.contentResolver.markItemAsRead(itemId)
                                cancelNotificationInBackground(appContext, itemId)
                            }
                        }
                    }
                }
            }

            /**
             * Called when a swipe is started.
             *
             * @param goingRight true if swiping to the right, false if left
             */
            override fun onSwipeStarted(goingRight: Boolean) {
                // SwipeRefreshLayout does not honor requestDisallowInterceptTouchEvent
                feedAdapter.feedFragment.swipeRefreshLayout!!.isEnabled = false

                val typedValue = TypedValue()
                if (PrefUtils.isNightMode(feedAdapter.feedFragment.activity!!)) {
                    feedAdapter.feedFragment.activity?.theme?.resolveAttribute(R.attr.nightBGColor,
                            typedValue, true)
                } else {
                    feedAdapter.feedFragment.activity?.theme?.resolveAttribute(android.R.attr.windowBackground,
                            typedValue, true)
                }
                bgFrame.setBackgroundColor(typedValue.data)
                checkBg.visibility = View.VISIBLE
                if (goingRight) {
                    checkLeft.visibility = View.VISIBLE
                } else {
                    checkRight.visibility = View.VISIBLE
                }
            }

            /**
             * Called when user doesn't swipe all the way.
             */
            override fun onSwipeCancelled() {
                // SwipeRefreshLayout does not honor requestDisallowInterceptTouchEvent
                feedAdapter.feedFragment.swipeRefreshLayout!!.isEnabled = true

                checkBg.visibility = View.INVISIBLE
                checkLeft.visibility = View.INVISIBLE
                checkRight.visibility = View.INVISIBLE

                bgFrame.background = null
            }

            /**
             * @return the subview which should move
             */
            override fun getSwipingView(): View {
                return bgFrame
            }
        }))
    }

    fun resetView() {
        checkBg.visibility = View.INVISIBLE
        checkLeft.visibility = View.INVISIBLE
        checkRight.visibility = View.INVISIBLE
        bgFrame.clearAnimation()
        bgFrame.alpha = 1.0f
        bgFrame.translationX = 0.0f
        bgFrame.background = null
    }

    fun fillTitle() {
        titleTextView.visibility = View.VISIBLE
        // \u2014 is a EM-dash, basically a long version of '-'
        feedAdapter.temps = if (rssItem!!.plainsnippet.isEmpty())
            rssItem!!.plaintitle
        else
            rssItem!!.plaintitle + " \u2014 " + rssItem!!.plainsnippet + "\u2026"
        val textSpan = SpannableString(feedAdapter.temps)

        textSpan.setSpan(TextAppearanceSpan(feedAdapter.feedFragment.context, R.style.TextAppearance_ListItem_Body),
                rssItem!!.plaintitle.length, feedAdapter.temps.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (rssItem!!.unread) {
            textSpan.setSpan(TextAppearanceSpan(feedAdapter.feedFragment.context, R.style.TextAppearance_ListItem_Title),
                    0, rssItem!!.plaintitle.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            textSpan.setSpan(TextAppearanceSpan(feedAdapter.feedFragment.context, R.style.TextAppearance_ListItem_Title_Read),
                    0, rssItem!!.plaintitle.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        titleTextView.text = textSpan
    }

    /**
     * OnItemClickListener replacement.
     *
     *
     * If a feeditem does not have any content,
     * then it opens the link in the browser directly.
     *
     * @param view
     */
    override fun onClick(view: View) {
        // Open item if not empty
        if (rssItem?.plainsnippet?.isNotEmpty() == true) {
            val i = Intent(feedAdapter.feedFragment.activity, ReaderActivity::class.java)
            i.putExtra(SHOULD_FINISH_BACK, true)
            rssItem?.let {
                ReaderActivity.setRssExtras(i, it)
            }

            feedAdapter.feedFragment.startActivity(i)
        } else {
            // Mark as read
            val contentResolver = feedAdapter.feedFragment.context?.contentResolver
            if (contentResolver != null) {
                val itemId = rssItem!!.id
                launch(Background) {
                    contentResolver.markItemAsRead(itemId)
                }
            }
            // Open in browser since no content was posted
            // Use enclosure or link
            feedAdapter.feedFragment.startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse(rssItem!!.enclosurelink ?: rssItem!!.link)))
        }
    }

    /**
     * Called when item has been measured, it is now the time to insert the image.
     *
     * @return Return true to proceed with the current drawing pass, or false to cancel.
     */
    override fun onPreDraw(): Boolean {
        if (!feedAdapter.feedFragment.isDetached && feedAdapter.feedFragment.activity != null) {
            try {
                GlideUtils.glide(feedAdapter.feedFragment.activity, rssItem!!.imageurl,
                        PrefUtils.shouldLoadImages(feedAdapter.feedFragment.activity!!))
                        .centerCrop()
                        .error(R.drawable.placeholder_image_list)
                        .into(imageView)
            } catch (e: IllegalArgumentException) {
                // Could still happen if we have a race-condition?
                Log.d(TAG, e.localizedMessage)
            }

        }

        // Remove as listener
        itemView.viewTreeObserver.removeOnPreDrawListener(this)
        return true
    }
}
